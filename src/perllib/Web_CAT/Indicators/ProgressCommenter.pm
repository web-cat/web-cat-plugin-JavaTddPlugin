#========================================================================
package Web_CAT::Indicators::ProgressCommenter;
#========================================================================

use warnings;
use strict;
use Data::Dumper;

our $VERSION = 0.2;

my %comments = (
  # aet: adding to existing tests
  'aet' => {
    'encourage' => [
      "Writing tests that cover all the scenarios helps you improve as a "
      . "programmer. I would recommend adding to your tests to account for "
      . "the different scenarios.",
      "Having tests that cover your entire program is a good way to ensure "
      . "your program is working as intended. See if you can add to your "
      . "tests to improve coverage.",
      "It is a good practice to test all the branches in your code. See if "
      . "you can add to your tests to cover more branches.",
    ],
    'reinforce' => [
      "I see that you have added more statements to your existing tests. It "
      . "is a good practice to test your code extensively.",
      "I observe that you have more statements in your tests than before. "
      . "Testing more scenarios is a good way to ensure that your program is "
      . "working as expected.",
      "I notice that you hvae added more statements to your existing tests. "
      . "Having good code coverage by adding more tests helpts build "
      . "confidence that your program works as intended.",
    ],
  },

  # asm: adding new solution methods
  'asm' => {
    'encourage' => [
      "A modularized solution will help you with code reuse and "
      . "maintainability. I encourage you to refactor your code into more "
      . "methods.",
      "Adding more methods to a solution helps to localize potential errors "
      . "and identify bugs quickly. I recommend splitting your functionality "
      . "among multiple methods.",
      "Wherever you see particularly large blocks of code, try to separate "
      . "the logic into multiple distinct methods. Modularizing your code in "
      . "this fashion will make your program easier to reuse and maintain.",
    ],
    'reinforce' => [
      "I see that you have added more methods to you solution classes. This "
      . "is a good practice to make the code more versatile and maintanable.",
      "I see more methods added to complete the solution. Adding more "
      . "methods to a solution helps to localize potential errors and "
      . "identify bugs quickly.",
      "I notice that you have extra methods in your solution classes "
      . "compared to before. This will help to make the code easier to reuse "
      . "and debug.",
    ],
  },

  # atm: adding new test methods
  'atm' => {
    'encourage' => [
      "Adding more methods to your tests enables you to change the behavior "
      . "of your program without making many modifications to your tests. "
      . "See if you can modularize your tests by adding more methods.",
      "Modularizing your test methods can be an effective way to quickly "
      . "identify failing scenarios. See if you can add more test methods "
      . "to break down the different scenarios.",
      "To make the behavior of your program more clear, try adding more "
      . "test methods to cover as many cases as possible.",
    ],
    'reinforce' => [
      "Having many test methods reflects that you know how your program "
      . "behaves given different conditions. Good job on adding new test "
      . "methods.",
      "I see you have added more methods to your test cases. Having more "
      . "test methods independent of the behavior of your program will "
      . "require less maintenance.",
      "I see that you have more test methods than before. Having different "
      . "methods to test different scenarios will help with identifying the "
      . "failing scenarios easily.",
    ],
  },

  # ccv: increasing conditional coverage
  'ccv' => {
    'encourage' => [
      "A great way to discover hidden bugs is to write tests which cover "
      . "all the statements and branches of your program. I would encourage "
      . "you to modify tests if they are not covering all the branches "
      . "currently.",
      "A good unit testing practice is to cover all of your conditions in "
      . "your program. If your tests are not covering all your conditions, "
      . "see if you can increase your tests to cover all of your conditions.",
      "I encourage you to check if your tests are covering all your "
      . "if-statements, methods, and loops and ensure your tests are "
      . "accounting for all possible inputs. This is a good way to ensure "
      . "your solution is working as expected.",
    ],
    'reinforce' => [
      "It is always smart to test every independent parth of your program "
      . "to identify potential bugs. I see that your software tests are "
      . "covering more conditional branches than before.",
      "Good job identifying and testing more branches that your program may "
      . "potentially take. This is an important practice in unit testing.",
      "Nice work hitting a greater percentage of your solution branches with "
      . "your software tests.",
    ],
  },

  # ias: increasing assertion density
  'ias' => {
    'encourage' => [
      "It is always a good practice to test your program for various input "
      . "conditions. See if you can add more assertions to test different "
      . "scenarios.",
      "I would suggest you add more assertions to see if different flows "
      . "in your program are triggered which were not triggering before.",
      "To make your software tests more effective, I recommend adding more "
      . "assertions to these tests that execute a larger percentage of your "
      . "code branches.",
    ],
    'reinforce' => [
      "I see you have added more assertions in your tests. Adding more "
      . "assertions is a good way to test your code against various "
      . "scenarios.",
      "I see that you have added more assertions to your tests to check "
      . "the correctness for different inputs to your code. Good work!",
      "Writing a thorough test to ensure your program is working in all "
      . "scenarios is a good unittesting practice. I see you have made "
      . "your software tests more effective by adding more assertions.",
    ],
  },

  # icd: increasing comment density
  'icd' => {
    'encourage' => [
      "I would recommend that you add more comments to your program to make "
      . "it easier to understand your logic.",
      "There appears to be large blocks of code in your program that are "
      . "not supplemented by any comments. I recommend adding comments to "
      . "these sections to make your code easier to follow.",
      "To make your program clearer to an outside reader, identify and "
      . "explain blocks of code throughout your solution that are missing "
      . "comments.",
    ],
    'reinforce' => [
      "I noticed that your program has more comments than before. If used "
      . "correctly, comments help with comprehensibility of your program.",
      "I see that your submission contains more comments than before. "
      . "Comments describing parts of your code helps people to better "
      . "understand your program and its intended behavior.",
      "I observe that your code has more comments than before. Comments, "
      . "when used properly, can make your program easier to follow.",
    ],
  },

  # ict: increasing correctness
  'ict' => {
    'encourage' => [
      "It looks like your code is not passing a few of the instructor-"
      . "provided tests. Pay closer attention to the requirements of the "
      . "project to pinpoint where your program is not behaving as desired.",
      "I see you are losing points for your code not passing instructor "
      . "reference tests. Check the assignment requirements carefully to "
      . "see how your program should behave.",
      "It appears there are still some reference tests you are not passing. "
      . "You can visit a TA during office hours to learn more about the "
      . "reference tests not passing.",
    ],
    'reinforce' => [
      "Good job on increasing your score by passing more instructor "
      . "provided reference tests.",
      "Your program is starting to conform more to the project's "
      . "requirements. Nice work!",
      "I see that your program is producing more of the desired behavior "
      . "stated in the project specification. Well done!",
    ],
  },

  # isc: increasing solution classes
  'isc' => {
    'encourage' => [
      "Object oriented programming is a powerful paradigm in which real "
      . "world objects are treated as separate classes. See if you can "
      . "refactor your code by adding more classes to match this real "
      . "world behavior.",
      "To improve the modularity of your solution, I recommend encapsulating "
      . "some of your program's functionality into additional classes.",
      "It appears you could use a few more classes in your program to "
      . "separate its various phases. This is a better design than lumping "
      . "all your functionality into a small number of classes.",
    ],
    'reinforce' => [
      "I see that you have added another class to your solution. Practicing "
      . "writing more classes will help make you a better object-oriented "
      . "programmer.",
      "I notice that there are more classes in your code than before. "
      . "Dispersing the functionality of your code among multiple classes "
      . "is always a good practice.",
      "I observe that you have more classes in your solution than before. "
      . "Having more types of objects to use makes your program easier "
      . "to conceptualize.",
    ],
  },

  # itc: increasing test classes
  'itc' => {
    'encourage' => [
      "Writing at least one test class for each class in your solution is "
      . "recommended practice. Practicing "
      . "writing more tests will help make you a better object-oriented "
      . "programmer.",
      "It doesn't appear that all of your classes have corresponding test "
      . "classes. Writing at least one test class for each class in your "
      . "solution is always a good practice.",
      "Adding at least one test class for every one in your solution will "
      . "help you think about how to self-check the code you are writing. "
      . "Having more tests makes your it easier to find problems in your "
      . "own work.",
    ],
    'reinforce' => [
      "I see that you have added another test class. Practicing "
      . "writing more tests will help make you a better object-oriented "
      . "programmer.",
      "I notice that there are more test classes in your code than before. "
      . "Writing at least one test class for each class in your solution "
      . "is always a good practice.",
      "I observe that you have more test classes than before. "
      . "Having more tests makes your it easier to find problems in your "
      . "own work.",
    ],
  },

  # mcv: increasing method coverage
  'mcv' => {
    'encourage' => [
      "Writing tests to cover more methods in your program is a good practice "
      . "to ensure your program is well tested. I would encourage you to "
      . "write tests to cover more of your methods if not done currently.",
      "If your tests are not covering all of your methods, I would recommend "
      . "you to write more tests covering all of your methods.",
      "Writing tests to cover all your methods is a good way to improve your "
      . "code coverage. See if you can write tests to cover all your "
      . "solution methods.",
    ],
    'reinforce' => [
      "It appears that your tests are now executing more of your methods. "
      . "Well done!",
      "I can see that you constructed your tests more carefully so that they "
      . "trigger more of your methods. This is a good programming practice!",
      "It appears that you have enhanced your tests by triggering a larger "
      . "number of your solution methods than before.",
    ],
  },

  # rcc: reducing cyclomatic complexity
  'rcc' => {
    'encourage' => [
      "If your tests are taking too long to write, you might want to consider "
      . "reducing the logic in your program that needs to be tested.",
      "I recommend you practice writing code with the minimum logic necessary "
      . "as it is a great way to improve your coding skill.",
      "If your logic in the code is overly complex, simplifying it will help "
      . "with ease of testing.",
    ],
    'reinforce' => [
      "I noticed that you have simplified some of the logic in your program. "
      . "Code with less complexity is easier to test.",
      "I see that you have removed some of the logic in your program. Less "
      . "complex code is always easier to understand.",
      "I observe that your solution is less complex than before. Avoiding "
      . "unnecessary branches and loop iterations is a great way to make "
      . "your code more understandable.",
    ],
  },

  # rms: reducing average method size
  'rms' => {
    'encourage' => [
      "Having smaller methods can improve the readability of the code. See "
      . "if you can make your methods smaller than they are now.",
      "Making your methods smaller will make it easier to identify "
      . "potential bugs. I would recommend you to check if you can make "
      . "your methods smaller.",
      "Having smaller methods makes it easier to test your code. Check if "
      . "there are ways to make your methods smaller.",
    ],
    'reinforce' => [
      "I observe that you have smaller methods than before. This will make "
      . "it easier to test your code.",
      "It appears that your methods contain fewer statements than before. "
      . "This is a good way to improve the readability of your code.",
      "I see that your methods generally contain fewer statements than "
      . "before. This is a good practice to exercise because it prevents "
      . "any one method from having too much capability.",
    ],
  },

  # rse: removing static analysis errors
  'rse' => {
    'encourage' => [
      "Fixing the style errors in your code can improve its readability. See "
      . "if you can improve the style of your code.",
      "Following standard coding conventions makes your code easier to work "
      . "on. I would recommend fixing some of the style errors shown here.",
      "Having clean, easy-to-read methods makes it easier to find bugs. Check "
      . "if there are ways to address some of the style errors in your "
      . "program.",
    ],
    'reinforce' => [
      "You've clearly reduced some of the coding style issues in your "
      . "program. This definitely improves the readability of your work.",
      "It appears that your program contains fewer coding style issues than "
      . "before. This is a good way to improve the readability of your code.",
      "I see that your methods contain fewer style problems than "
      . "before. This is a good practice to exercise because it helps "
      . "make your code easier to understand and navigate.",
    ],
  },

  # scv: increasing statement coverage
  'scv' => {
    'encourage' => [
      "A great way to discover hidden bugs is to write tests which cover "
      . "all the statements and branches of your program. I would encourage "
      . "you to modify tests to exercise all of the statements in your "
      . "solution.",
      "A good unit testing practice is to cover all of the statements in "
      . "your program. If your tests are not executing all of your "
      . "statements, see if you can add to your tests to cover them.",
      "I encourage you to check if your tests are covering all your "
      . "if-statements, methods, and loops and ensure your tests are "
      . "accounting for all possible inputs. This is a good way to ensure "
      . "your solution is working as expected.",
    ],
    'reinforce' => [
      "It is always smart to test every line of your program "
      . "to identify potential bugs. I see that your software tests are "
      . "covering more statements than before.",
      "Good job identifying and testing more statements in your code. "
      . " This is an important practice in unit testing.",
      "Nice work hitting a greater percentage of your program statements "
      . "with your software tests.",
    ],
  },

  # tpm: increasing number of tests per method
  'tpm' => {
    'encourage' => [
      "Writing tests that cover all the scenarios helps you improve as a "
      . "programmer. I would recommend adding to your tests to account for "
      . "the different scenarios.",
      "Having tests that cover your entire program is a good way to ensure "
      . "your program is working as intended. See if you can add to your "
      . "tests to improve coverage.",
      "Adding more methods to your tests enables you to change the behavior "
      . "of your program without making many modifications to your tests. "
      . "See if you can modularize your tests by adding more methods.",
      "Modularizing your test methods can be an effective way to quickly "
      . "identify failing scenarios. See if you can add more test methods "
      . "to break down the different scenarios.",
      "To make the behavior of your program more clear, try adding more "
      . "test methods to cover as many cases as possible.",
    ],
    'reinforce' => [
      "I see that you have added more tests. It "
      . "is a good practice to test your code extensively.",
      "I observe that you have more test methods than before. "
      . "Testing more scenarios is a good way to ensure that your program is "
      . "working as expected.",
      "I notice that you hvae added more tests. "
      . "Having good code coverage by adding more tests helpts build "
      . "confidence that your program works as intended.",
      "Having many test methods reflects that you know how your program "
      . "behaves given different conditions. Good job on adding new test "
      . "methods.",
      "I see you have added more methods to your test cases. Having more "
      . "test methods independent of the behavior of your program will "
      . "require less maintenance.",
      "I see that you have more test methods than before. Having different "
      . "methods to test different scenarios will help with identifying the "
      . "failing scenarios easily.",
    ],

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
                    'mostRecent.progressCommenter.results', '');
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
        subnum       => $num,
        timestamp    => $timestamp,
        feedbackTime => 0,
        nextType     => 'reinforce',
        };
    my $last = $#{$self->{histories}};
    if ($last >= 0)
    {
        $self->update_current_from($self->{histories}[$last]);
    }

    # If we need to generate a new feedbackTime
    if ($timestamp > $self->{current}->{feedbackTime})
    {
        # If we actually need to generate feedback this round
        if ($self->{current}->{feedbackTime} > 0 || $num > 4)
        {
            $self->force_show($self->{current}->{nextType});
        }
        else
        {
            $self->force_new_feedback_delay;
        }
    }
    push(@{$self->{histories}}, $self->{current});
}


#========================================================================
sub force_show
{
    my $self = shift;
    my $type = shift;

    $self->{current}->{show} = 1;
    $self->{current}->{type} = $type;
    $self->{current}->{nextType} =
        ($self->{current}->{type} eq 'reinforce')
        ? 'encourage'
        : 'reinforce';
    $self->force_new_feedback_delay;
}


#========================================================================
sub force_new_feedback_delay
{
    my $self = shift;
    my $timestamp = $self->{current}->{timestamp};
    my $feedbackDelay = int((30 + rand(60)) * 60 * 1000);
    $self->{current}->{feedbackDelay} = $feedbackDelay;
    $self->{current}->{feedbackTime} = $timestamp + $feedbackDelay;
}


#========================================================================
sub update_current_from
{
    my $self = shift;
    my $last = shift;

    $self->{current}->{show} = 0;
    if (defined $last->{feedbackTime})
    {
        $self->{current}->{feedbackTime} = $last->{feedbackTime};
    }
    if (defined $last->{nextType})
    {
        $self->{current}->{nextType} = $last->{nextType};
    }
    if (defined $last->{prevIndicator})
    {
        $self->{current}->{prevIndicator} = $last->{prevIndicator};
    }
    if (defined $last->{type})
    {
        $self->{current}->{prevType} = $last->{type};
    }
    elsif (defined $last->{prevType})
    {
        $self->{current}->{prevType} = $last->{prevType};
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
    $self->{cfg}->setProperty('progressCommenter.results', $self->toString);
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
sub comment
{
    my $self = shift;
    my @triggered_indicators = @_;

    if ($#triggered_indicators < 0 && defined $self->{tracker})
    {
        # @triggered_indicators = $self->{tracker}->triggered_indicators;
        @triggered_indicators = $self->{tracker}->improved_indicators;
    }

    my $size = scalar @triggered_indicators;
    my $type = $self->{current}->{type} || 'reinforce';
    $self->{current}->{triggered} = 0;

    if ($size == 0)
    {
        $type = 'encourage';
    }
    if ($size >= 4)
    {
        my @hard_indicators = $self->{tracker}->triggered_indicators;
        if (scalar @hard_indicators >= 4)
        {
            @triggered_indicators = @hard_indicators;
            $self->{current}->{triggered} = 1;
            $type = 'reinforce';
            $self->force_show($type);
        }
    }

    if ($type eq 'encourage')
    {
        # Get all indicators that were not triggered
        my @difference = ();
        my %triggered = map { $_ => 1 } @triggered_indicators;
        foreach my $indicator (@Web_CAT::Indicators::ProgressTracker::INDICATORS)
        {
            if (!defined $triggered{$indicator}
                && $self->{tracker}->encouragement_elligible($indicator))
            {
                push @difference, $indicator;
            }
        }
        @triggered_indicators = @difference;
        $size = scalar @triggered_indicators;
    }

    my $comment = undef;
    if ($self->{current}->{show})
    {
        my $indnum = int(rand($size));
        my $indicator = $triggered_indicators[$indnum];
        $self->{current}->{indicator} = $indicator;
        $self->{current}->{prevIndicator} = $indicator;
        $self->{current}->{type} = $type;
    
        if (scalar @{$comments{$indicator}->{$type}} > 0)
        {
            my $comment_number =
                int(rand(@{$comments{$indicator}->{$type}}));
            $self->{current}->{num} = $comment_number;
            $comment = $comments{$indicator}->{$type}->[$comment_number];
        }
    }

    $self->{current}->{comment} = $comment;
    return $comment;
}



# ---------------------------------------------------------------------------
1;
# ---------------------------------------------------------------------------
