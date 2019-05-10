#========================================================================
package Web_CAT::Indicators::DailyMissionGenerator;
#========================================================================

use warnings;
use strict;
use Data::Dumper;

our $VERSION = 0.1;

my %missions = (
  #  'asm',  # adding new solution methods
  'asm' => {
    'description' => '<b>Add more methods</b> to your solution, to make your '
        . 'methods shorter and easier to understand (to increase readability '
        . 'and improve method design)',
  },
  
  #  'rse',  # removing static analysis errors
  'rse' => {
    'description' => '<b>Fix coding style</b> and documentation errors (to increase '
        . 'your score)',
  },
  
  #  'rcc',  # reducing cyclomatic complexity
  'rcc' => {
    'description' => '<b>Simplify the logic</b> in your solution by removing or '
        . 'consolidating if statements or loops (to increase readability '
        . 'and testability, and reduce potential for bugs)',
  },
  
  #  'rms',  # reducing average method size
  'rms' => {
    'description' => '<b>Reduce the length of your methods</b> by breaking them '
        . 'into more manageable pieces (to increase readability and '
        . 'improve method design)',
  },
  
  #  'icd',  # increasing comment density
  'icd' => {
    'description' => '<b>Add comments</b> to your solution (to increase readability)',
  },
  
  #  'isc',  # increasing solution classes
  'isc' => {
    'description' => '<b>Add another class</b> to your solution (for better self-checking)',
  },
  
  #  'ict',  # increasing correctness
  'ict' => {
    'description' => '<b>Pass one or more additional reference tests</b> by '
        . 'improving your program\'s behavioral correctness (to improve your score)',
  },
  
  #  'atm',  # adding new test methods
  'atm' => {
    'description' => '<b>Add new test methods</b> to increase your '
        . 'self-checking (for better self-checking)',
  },
  
  #  'aet',  # adding to existing tests
  'aet' => {
    'description' => '<b>Add to your software tests</b> to increase your '
        . 'self-checking (for better self-checking)',
  },
  
  #  'tpm',  # increasing number of tests per method
  'tpm' => {
    'description' => '<b>Add new test methods</b> to increase your '
        . 'self-checking (for better self-checking)',
  },
  
  #  'scv',  # increasing statement coverage
  'scv' => {
    'description' => 'Add to your software tests to <b>exercise more of the '
        . 'statements</b> in your solution (for better self-checking and to '
        . 'improve your score)',
  },
  
  #  'mcv',  # increasing method coverage
  'mcv' => {
    'description' => 'Add to your software tests to <b>exercise more of the '
        . 'methods</b> in your solution (for better self-checking and to '
        . 'improve your score)',
  },
  
  #  'ccv',  # increasing conditional coverage
  'ccv' => {
    'description' => 'Add to your software tests to <b>exercise more of the '
        . 'logic conditions</b> in your solution (for better self-checking and to '
        . 'improve your score)',
  },
  
  #  'ias',  # increasing assertion density
  'ias' => {
    'description' => '<b>Add more assertions</b> to your software tests so they '
        . 'check expected behaviors more comprehensively (for better '
        . 'self-checking)',
  },
  
  #  'itc',  # increasing test classes
  'itc' => {
    'description' => '<b>Add another test class</b> (for better self-checking)',
  },
  
);


#========================================================================
#                      -----  PUBLIC METHODS -----
#========================================================================

#========================================================================
sub new
{
    my $class = shift;
    my $tracker = shift;
    my $initial = undef;
    my $cfg = undef;
    if (defined $tracker)
    {
        if (blessed $tracker)
        {
            $cfg = $tracker->config;
            if (defined $cfg)
            {
                $initial = $cfg->getProperty(
                    'mostRecent.dailyMissions.results', '');
            }
        }
        else
        {
            $initial = $tracker;
            $tracker = undef;
        }
    }
    my $self = {
        histories => (defined $initial && $initial ne '')
            ? eval($initial)
            : [],
        current => undef,
        tracker => $tracker,
        cfg     => $cfg,
    };

    bless $self, $class;
    if (defined $self->{cfg})
    {
        $self->add_submission(
            $self->{cfg}->getProperty('submissionNo'),
            $self->{cfg}->getProperty('submissionTimestamp')
            );
    }
    return $self;
}


#========================================================================
sub add_submission
{
    my $self = shift;
    my $num = shift;
    my $timestamp = shift;

    $self->{current} = {
        subnum          => $num,
        timestamp       => $timestamp,
        missionTurnover => 0,
        };
    my $last = $#{$self->{histories}};
    if ($last >= 0)
    {
        $self->update_current_from($self->{histories}[$last]);
    }

    # If we need to generate a new feedbackTime
    if ($timestamp > $self->{current}->{missionTurnover}
        || !defined $self->{current}->{missions})
    {
        $self->force_new_missions;
    }
    else
    {
        $self->update_mission_achievements;
    }
    push(@{$self->{histories}}, $self->{current});
}


#========================================================================
sub force_new_missions
{
    my $self = shift;
    my $timestamp = $self->{current}->{timestamp};
    my $today = int($timestamp / (24 * 60 * 60 * 1000))
        * (24 * 60 * 60 * 1000) + (4 * 60 * 60 * 1000); # add EDT time offset 
    my $missionTurnover = $today + (24 * 60 * 60 * 1000);
    $self->{current}->{missionTurnover} = $missionTurnover;
    $self->{current}->{missions} = $self->generate_new_missions;
    $self->{current}->{pop} = 1;
    if (defined $self->{cfg})
    {
        my $runtimeScore = 
            $self->{cfg}->getProperty('score.correctness', '0');
        my $staticScore =
            $self->{cfg}->getProperty('score.tools', '0');
        $self->{current}->{base_correctness_score} = $runtimeScore;
        $self->{current}->{base_static_score} = $staticScore;
        $self->{current}->{base_score} = $runtimeScore + $staticScore;
    }
}


#========================================================================
sub update_current_from
{
    my $self = shift;
    my $last = shift;

    for my $key ('missionTurnover',
        'base_correctness_score',
        'base_static_score')
    {
        if (defined $last->{$key})
        {
            $self->{current}->{$key} = $last->{$key};
        }
    }
    $self->{current}->{pop} = 0;
    if (defined $last->{missions})
    {
        $self->{current}->{missions} = eval(
            Data::Dumper->new([$last->{missions}])->Terse(1)->Indent(0)->Dump);
        for my $mission (@{$self->{current}->{missions}})
        {
            if ($mission->{state} == 1)
            {
                $mission->{state} = 2;
            }
        }
    }
}


#========================================================================
sub generate_new_missions
{
    my $self = shift;
    my $missions = [];
    my @candidates = ();
    foreach my $indicator (@Web_CAT::Indicators::ProgressTracker::INDICATORS)
    {
        if ($self->{tracker}->encouragement_elligible($indicator))
        {
            push @candidates, $indicator;
        }
    }
    print "candidates for missions: ", join(', ', @candidates), "\n";
    my %candidate_map = map {$_ => 1} @candidates;
    my $size = scalar @candidates;
    if (defined $self->{current}->{missions})
    {
        # First, remove missions completed yesterday
        for my $mission (@{$self->{current}->{missions}})
        {
            if ($mission->{state} > 0 && $size > 5)
            {
                delete $candidate_map{$mission->{indicator}};
                $size--;
            }
        }
        
        # Filter out near-duplicate missions
        if (exists $candidate_map{'mcv'} && $size > 5)
        {
            # Has method coverage as an option, so remove others
            if (exists $candidate_map{'ccv'} && $size > 5)
            {
                delete $candidate_map{'ccv'};
                $size--;
            }
            if (exists $candidate_map{'scv'} && $size > 5)
            {
                delete $candidate_map{'scv'};
                $size--;
            }
        }
        elsif (exists $candidate_map{'scv'} && $size > 5)
        {
            # Has method coverage as an option, so remove others
            if (exists $candidate_map{'ccv'} && $size > 5)
            {
                delete $candidate_map{'ccv'};
                $size--;
            }
        }
        {
            # Reduce testing missions to just one
            my @testingMissions = ('ias', 'atm', 'tpm', 'aet', 'itc');
            my %testingIndicators = map { $_ => 1 } @testingMissions;
            my @remove = ();
            for my $mission (@{$self->{current}->{missions}})
            {
                if (exists $testingIndicators{$mission->{indicator}})
                {
                    delete $testingIndicators{$mission->{indicator}};
                    push @remove, $mission->{indicator};
                }
            }
            @testingMissions = keys %testingIndicators;
            for my $indicator (@testingMissions)
            {
                if (!exists $candidate_map{$indicator})
                {
                    delete $testingIndicators{$indicator};
                }
            }
            @testingMissions = keys %testingIndicators;
            if (0 < scalar @testingMissions)
            {
                my $indnum = int(rand($size));
                my $indicator = $candidates[$indnum];
                push @testingMissions, @remove;
                for my $i (@testingMissions)
                {
                    if ($i ne $indicator
                        && exists $candidate_map{$i} 
                        && $size > 5)
                    {
                        delete $candidate_map{i};
                        $size--;
                    }
                }
            }
        }

        # Next, remove missions from yesterday not completed
        for my $mission (@{$self->{current}->{missions}})
        {
            if ($mission->{state} == 0 && $size > 5)
            {
                delete $candidate_map{$mission->{indicator}};
                $size--;
            }
        }
    }
    
    my $missionCount = 0;
    if (exists $candidate_map{'ict'})
    {
        $missionCount++;
        push(@{$missions}, {
            'indicator' => 'ict',
            'state' => 0,
            'target' => $self->{tracker}->indicator('ict') || 0,
            });
        delete $candidate_map{'ict'};
    }
    @candidates = keys %candidate_map;
    print "filtered candidates for missions: ", join(', ', @candidates), "\n";
    $size = scalar @candidates;
    for (; $missionCount < 5 && $size > 0; $missionCount++)
    {
        my $indnum = int(rand($size));
        my $indicator = $candidates[$indnum];
        if (exists $candidate_map{$indicator})
        {
            push(@{$missions}, {
                'indicator' => $indicator,
                'state' => 0,
                'target' => $self->{tracker}->indicator($indicator) || 0,
                 });
            delete $candidate_map{$indicator};
            @candidates = keys %candidate_map;
            $size = scalar @candidates;
        }
        else
        {
            $missionCount--;
        }
    }    

    if (scalar @{$missions} < 5)
    {
        print "warning: fewer than 5 missions generated:\n",
            Dumper($missions), "\n";
    }
    return $missions;
}


#========================================================================
sub update_mission_achievements
{
    my $self = shift;
    for my $mission (@{$self->{current}->{missions}})
    {
        if ($mission->{state} == 0)
        {
            my $v = $self->{tracker}->indicator($mission->{indicator});
            if (defined $v &&
                Web_CAT::Indicators::ProgressTracker::m_is_better(
                    $mission->{indicator},
                    $v,
                    $mission->{target}))
            {
                $mission->{state}++;
            }
        }
    }
}


#========================================================================
sub toString
{
    my $self = shift;

    return Data::Dumper->new([$self->{histories}])
        ->Terse(1)->Indent(0)->Dump; # ->Useqq(1)
}


#========================================================================
sub save
{
    my $self = shift;
    if (defined $self->{tracker})
    {
        $self->{tracker}->save;
    }
    $self->{cfg}->setProperty('dailyMissions.results', $self->toString);
}


#========================================================================
sub histories
{
    my $self = shift;
    return $self->{histories}; 
}


#========================================================================
sub history
{
    my $self = shift;
    return $self->{current};
}


#========================================================================
sub render_missions
{
    my $self = shift;

    my $result = <<MESSAGE;
<div id="dailyMissionsDialog" dojoType="webcat.Dialog" title="Today's Daily Missions">
  <div>
    Earn a full recharge of submission energy for any mission completed:
    <ul class="checklist box">
MESSAGE
#    <li class="complete">First mission</li>
#    <li class="complete"><span class="label label-success">Just Completed</span> Older mission</li>
#    <li class="incomplete"><span class="label label-info">Earn Full Recharge</span>Not yet on this one</li>

    my $missionsEarned = 0;
    my $finalWord = '';
    for my $mission (@{$self->{current}->{missions}})
    {
        my $state = $mission->{state};
        my $css = 'complete';
        if ($state == 0) { $css = 'in' . $css; }
        my $label = '';
        my $endlabel = '';
        if ($state == 1)
        {
            $missionsEarned++;
            if (defined $self->{cfg})
            {
                $self->{cfg}->setProperty('submission.energy.recharge',
                    $missionsEarned);
                $self->{current}->{pop}++;
                $self->{cfg}->setProperty('dailyMissions.show.results',
                    $self->{current}->{pop});
            }
            $label = '<span class="alert-success">';
        }
        $result .= '<li class="' . $css . '">' . $label
            . $missions{$mission->{indicator}}->{description}
            . $endlabel . '</li>';
    }
    if ($missionsEarned > 0)
    {
        $finalWord .= '<div class="alert alert-success">You have completed '
            . '<strong>' . $missionsEarned
            . ' new mission'
            . (($missionsEarned == 1) ? '' : 's')
            . '</strong>! Your submission energy has been fully '
            . 'recharged.</div>';
    }
    if (defined $self->{cfg} && defined $self->{current}->{base_score})
    {
        my $runtimeScore = 
            $self->{cfg}->getProperty('score.correctness', '0');
        my $staticScore =
            $self->{cfg}->getProperty('score.tools', '0');
        if ($self->{current}->{base_score} < $runtimeScore + $staticScore)
        {
            my $gain = $runtimeScore + $staticScore
                - $self->{current}->{base_score};
            if ($gain >= 1)
            {
                $finalWord .= '<div class="alert alert-success">You have '
                    . 'increased your score by '
                    . '<strong>' . $gain
                    . ' point'
                    . (($gain == 1) ? '' : 's')
                    . '</strong>!</div>';
            }
        }
    }
    $result .= <<MESSAGE;
    </ul>$finalWord
  </div>
  <div style="text-align: center">
    <button id="missionokbutton" dojoType="dijit.form.Button" type="button" class="pos">OK
      <script type="dojo/connect" event="onClick">
        dijit.byId('dailyMissionsDialog').hide();
      </script>
    </button>
  </div>
</div>
MESSAGE
    if ($self->{current}->{pop})
    {
        $result .= <<MESSAGE;
<script type="text/javascript">
dojo.addOnLoad(function() {
  dijit.byId('dailyMissionsDialog').show();
});
</script>
MESSAGE
    }

    # print "rendered missions:\n", $result, "\n";
    return $result;
}

# ---------------------------------------------------------------------------
1;
# ---------------------------------------------------------------------------
