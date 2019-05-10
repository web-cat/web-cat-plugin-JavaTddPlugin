#========================================================================
package Web_CAT::Indicators::ProgressTracker;
#========================================================================

use warnings;
use strict;
use Data::Dumper;
use Scalar::Util qw(blessed);
use Config::Properties::Simple;
use List::Util qw(min max);
use Carp qw(confess);

our $VERSION = 0.2;
our @INDICATORS = (
  'asm',  # adding new solution methods
  'rse',  # removing static analysis errors
  'rcc',  # reducing cyclomatic complexity
  'rms',  # reducing average method size
  'icd',  # increasing comment density
  'isc',  # increasing solution classes
  'ict',  # increasing correctness
  'atm',  # adding new test methods
  'aet',  # adding to existing tests
  'tpm',  # increasing number of tests per method
  'scv',  # increasing statement coverage
  'mcv',  # increasing method coverage
  'ccv',  # increasing conditional coverage
  'ias',  # increasing assertion density
  'itc',  # increasing test classes
  );
our $HISTORY_WINDOW_SIZE = 20;
our $COMPARISON_WINDOW_SIZE = 4;

my @progress_tags = (
  @INDICATORS,
  'subnum',     # submission number 
  'timestamp',  # submission timestamp
  'triggered',  # array of triggered indicators
  );

my %compareop = (
  'asm' => \&mmax,  # adding new solution methods
  'rse' => \&mmin,  # removing static analysis errors
  'rcc' => \&mmin,  # reducing cyclomatic complexity
  'rms' => \&mmin,  # reducing average method size
  'icd' => \&mmax,  # increasing comment density
  'isc' => \&mmax,  # increasing solution classes
  'ict' => \&mmax,  # increasing correctness
  'atm' => \&mmax,  # adding new test methods
  'aet' => \&mmax,  # adding to existing tests
  'tpm' => \&mmax,  # increasing number of tests per method
  'scv' => \&mmax,  # increasing statement coverage
  'mcv' => \&mmax,  # increasing method coverage
  'ccv' => \&mmax,  # increasing conditional coverage
  'ias' => \&mmax,  # increasing assertion density
  'itc' => \&mmax,  # increasing test classes
  );


#========================================================================
#                      -----  PUBLIC METHODS -----
#========================================================================

#========================================================================
sub new
{
    my $class = shift;
    my $cfg = shift;
    my $initial = undef;
    if (defined $cfg)
    {
        if (blessed $cfg)
        {
            $initial =
                $cfg->getProperty('mostRecent.progressTracker.results', '');
        }
        else
        {
            $initial = $cfg;
            $cfg = undef;
        }
    }
#    my $self = (defined $initial && $initial ne '')
#        ? eval($initial)
#        : {
#        histories => [],
#    };
    my $self = {
		histories => (defined $initial && $initial ne '')
	        ? eval($initial)
	        : [],
	    current => undef,
	    cfg => $cfg,
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
    $self->{current} = { subnum => $num, timestamp => $timestamp };
    push(@{$self->{histories}}, $self->{current});
}


#========================================================================
sub toString
{
    my $self = shift;
    if (defined $self->{current} && ! defined $self->{current}->{triggered})
    {
        $self->triggered_indicators;
    }
    if (defined $self->{current} && ! defined $self->{current}->{improved})
    {
        $self->improved_indicators;
    }

    # return Data::Dumper->new([{%{ $self }}])
    return Data::Dumper->new([$self->{histories}])
        ->Terse(1)->Indent(0)->Dump; # ->Useqq(1)
}


#========================================================================
sub save
{
    my $self = shift;
    $self->{cfg}->setProperty('progressTracker.results', $self->toString);
}


#========================================================================
sub set_indicator
{
    my $self = shift;
    my $indicator = shift;
    my $value = shift;

    $self->{current}->{$indicator} = $value;
}


#========================================================================
sub indicator
{
    my $self = shift;
    my $indicator = shift;

    return $self->{current}->{$indicator};
}


#========================================================================
sub triggered_indicators
{
    my $self = shift;
    my @triggered = ();
    foreach my $indicator (@INDICATORS)
    {
        if ($self->is_triggered($indicator))
        {
            push @triggered, $indicator;
        }
    }
    $self->{current}->{triggered} = [@triggered];
    return @triggered;
}


#========================================================================
sub improved_indicators
{
    my $self = shift;
    my @triggered = ();
    foreach my $indicator (@INDICATORS)
    {
        if ($self->is_triggered($indicator, 1, 1))
        {
            push @triggered, $indicator;
        }
    }
    $self->{current}->{improved} = [@triggered];
    return @triggered;
}


#========================================================================
sub is_triggered
{
    my $self = shift;
    my $indicator = shift;
    my $window_size = shift || $COMPARISON_WINDOW_SIZE;
    my $allow_missing = shift || 0;
    my $current_val = $self->{current}->{$indicator};
    if (!defined $current_val)
    {
        return $current_val;
    }
    my @comparison_vals = $self->comparison_values($indicator, $window_size);

    my $num_vals = scalar @comparison_vals;
    if ($allow_missing && $num_vals == 0)
    {
        @comparison_vals = (0);
        $num_vals = 1;
    }
    return ($num_vals >= $window_size || $allow_missing)
        && $compareop{$indicator}->($current_val, @comparison_vals);
}


#========================================================================
sub comparison_values
{
    my $self = shift;
    my $indicator = shift;
    my $window_size = shift || $COMPARISON_WINDOW_SIZE;
    my $size = $#{$self->{histories}} - 1;
    my $numfinds = 0;
    my @values = ();
    for (my $i = $size; $i >= 0 && $numfinds < $window_size; $i--)
    {
        if( defined $self->{histories}[$i]->{$indicator})
        {
            push @values, $self->{histories}[$i]->{$indicator};
        }
    }
    return @values;
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
sub config
{
    my $self = shift;
    return $self->{cfg};
}


#========================================================================
sub tags
{
    return @progress_tags;
}


#========================================================================
sub mmin
{
    my $current_val = shift;
    return ($current_val < min(@_));
}


#========================================================================
sub mmax
{
    my $current_val = shift;
    return ($current_val > max(@_));
}


#========================================================================
sub m_is_better
{
    my $indicator = shift;
    if (scalar @_ == 1)
    {
        push(@_, 0); 
    }
    return $compareop{$indicator}->(@_);
}


#========================================================================
sub encouragement_elligible
{
    my $self = shift;
    my $indicator = shift;
    my $result = 1;

    if ($indicator eq 'rse')  # removing static analysis errors
    { $result = (defined($self->indicator($indicator))
        && $self->indicator($indicator) > 0); }
    elsif ($indicator eq 'rcc')  # reducing cyclomatic complexity
    { $result = (!defined($self->indicator($indicator))
        || defined($self->indicator('asm'))
        && $self->indicator($indicator)
        > 2 * $self->indicator('asm')); }
    elsif ($indicator eq 'rms')  # reducing average method size
    { $result = (defined($self->indicator($indicator))
        && $self->indicator($indicator) > 9); }
    elsif ($indicator eq 'icd')  # increasing comment density
    { $result = (defined($self->indicator($indicator))
        && $self->indicator($indicator) < 0.5); }
    elsif ($indicator eq 'isc')  # increasing solution classes
    # TODO: Add programmable limit for this
    { $result = 0; }
    elsif ($indicator eq 'ict')  # increasing correctness
    { $result = (!defined($self->indicator($indicator))
        || $self->indicator($indicator) < 1); }
    elsif ($indicator eq 'atm')  # adding new test methods
    { $result = (!defined($self->indicator($indicator))
        || defined($self->indicator('asm'))
        && $self->indicator($indicator)
        < 1.1 * $self->indicator('asm')); }
    elsif ($indicator eq 'tpm')  # increasing number of tests per method
    # suppress, since it overlaps with atm
    { $result = 0; }
    elsif ($indicator eq 'scv')  # increasing statement coverage
    { $result = (defined($self->indicator($indicator))
        && $self->indicator($indicator) < 1); }
    elsif ($indicator eq 'mcv')  # increasing method coverage
    { $result = (!defined($self->indicator($indicator))
        || $self->indicator($indicator) < 1); }
    elsif ($indicator eq 'ccv')  # increasing conditional coverage
    { $result = (defined($self->indicator($indicator))
        && $self->indicator($indicator) < 1); }
    elsif ($indicator eq 'itc')  # increasing test classes
    { $result = (defined($self->indicator($indicator))
        && $self->indicator($indicator)
        < $self->indicator('isc')); }

    return $result;
}


#========================================================================
sub collect_indicators
{
    my $self = shift;
    
    if (!defined $self->{cfg})
    {
        confess 'config is required but not set';
    }

    my @codeMarkupKeys = (
        'elements',
        'elementsCovered',
        'statements',
        'statementsCovered',
        'conditionals',
        'conditionalsCovered',
        'methods',
        'methodsCovered',
#        'sourceFileName',
#        'deductions',
        'remarks',
#        'pkgName',
#        'className',
        'loc',
        'ncloc',
        'tests',
        'asserts',
    );

    my %totals = (
        'solution' => {
            'classes' => 0,
            'methods' => 0,
            'methodsCovered' => 0,
            'statements' => 0,
            'statementsCovered' => 0,
            'conditionals' => 0,
            'conditionalsCovered' => 0,
            'elements' => 0,
            'elementsCovered' => 0,
            'remarks' => 0,
            'loc' => 0,
            'ncloc' => 0,
            'tests' => 0,
            'asserts' => 0,
        },
        'test' => {
            'classes' => 0,
            'methods' => 0,
            'methodsCovered' => 0,
            'statements' => 0,
            'statementsCovered' => 0,
            'conditionals' => 0,
            'conditionalsCovered' => 0,
            'elements' => 0,
            'elementsCovered' => 0,
            'remarks' => 0,
            'loc' => 0,
            'ncloc' => 0,
            'tests' => 0,
            'asserts' => 0,
        },
        'both' => {
            'classes' => 0,
            'methods' => 0,
            'methodsCovered' => 0,
            'statements' => 0,
            'statementsCovered' => 0,
            'conditionals' => 0,
            'conditionalsCovered' => 0,
            'elements' => 0,
            'elementsCovered' => 0,
            'remarks' => 0,
            'loc' => 0,
            'ncloc' => 0,
            'tests' => 0,
            'asserts' => 0,
        }
    );
    my $numCodeMarkups = $self->{cfg}->getProperty('numCodeMarkups');
    my $cfg = $self->{cfg};
    for (my $i = 1; $i <= $numCodeMarkups; $i++)
    {
        my $className = $cfg->getProperty('codeMarkup' . $i . '.className');
        next if (!defined $className || $className eq '');

        my $type = ($className =~ /Test(s?)$/) ? 'test' : 'solution';
        $totals{$type}->{'classes'}++;
        $totals{'both'}->{'classes'}++;

        for my $k (@codeMarkupKeys)
        {
            my $kk = 'codeMarkup' . $i . '.' . $k;
            if (defined $cfg->getProperty($kk))
            {
                $totals{$type}->{$k} += $cfg->getProperty($kk);
                $totals{'both'}->{$k} += $cfg->getProperty($kk);
            }
        }
    }
    # print "totals:\n", Dumper(\%totals), "\n";

    # asm: solution methods
    $self->set_indicator('asm', $totals{'solution'}->{'methods'});
    # mcv: method coverage
    {
        my $methods = $totals{'solution'}->{'methods'};
        my $covered = $totals{'solution'}->{'methodsCovered'};
        my $coverage = ($methods == 0) ? 0 : ($covered / $methods);
        $self->set_indicator('mcv', $coverage);
    }
    # rse: static analysis errors
    $self->set_indicator('rse', $totals{'both'}->{'remarks'});
    # rcc: cyclomatic complexity
    $self->set_indicator('rcc', $totals{'solution'}->{'methods'}
        + $totals{'solution'}->{'conditionals'});
    # rms: average method size
    {
        my $methods = $totals{'solution'}->{'methods'};
        my $lines = $totals{'solution'}->{'ncloc'};
        my $avg = ($methods == 0) ? 0 : ($lines / $methods);
        $self->set_indicator('rms', $avg);
    }
    # icd: increasing comment density
    {
        my $lines = $totals{'solution'}->{'loc'};
        my $comments = $lines - $totals{'solution'}->{'ncloc'};
        my $avg = ($lines == 0) ? 0 : ($comments / $lines);
        $self->set_indicator('icd', $avg);
    }
    # isc: increasing solution classes
    $self->set_indicator('isc', $totals{'solution'}->{'classes'});
    # ict: increasing correctness
    {
        my $tests = $cfg->getProperty('instructor.test.executed');
        my $passed = $cfg->getProperty('instructor.test.passed');
        if (defined $tests && $tests ne '')
        {
            my $avg = ($tests == 0) ? 0 : ($passed / $tests);
            $self->set_indicator('ict', $avg);
        }
    }
    # atm: adding new test methods
    $self->set_indicator('atm', $totals{'test'}->{'tests'});
    # aet: adding to existing tests
    $self->set_indicator('aet', $totals{'test'}->{'ncloc'});
    # tpm: increasing number of tests per method
    {
        my $methods = $totals{'solution'}->{'methods'};
        my $tests = $totals{'test'}->{'tests'};
        my $avg = ($methods == 0) ? 0 : ($tests / $methods);
        $self->set_indicator('tpm', $avg);
    }
    # scv: statement coverage
    {
        my $statements = $totals{'solution'}->{'statements'};
        my $covered = $totals{'solution'}->{'statementsCovered'};
        my $coverage = ($statements == 0) ? 0 : ($covered / $statements);
        $self->set_indicator('scv', $coverage);
    }
    # ccv: increasing conditional coverage
    {
        my $conditionals = $totals{'solution'}->{'conditionals'};
        my $covered = $totals{'solution'}->{'conditionalsCovered'};
        my $coverage = ($conditionals == 0) ? 0 : ($covered / $conditionals);
        $self->set_indicator('ccv', $coverage);
    }
    # ias: increasing assertion density
    {
        my $methods = $totals{'test'}->{'tests'};
        my $asserts = $totals{'test'}->{'asserts'};
        my $avg = ($methods == 0) ? 0 : ($asserts / $methods);
        $self->set_indicator('ias', $avg);
    }
    # itc: increasing test classes
    $self->set_indicator('itc', $totals{'test'}->{'classes'});
}


# ---------------------------------------------------------------------------
1;
# ---------------------------------------------------------------------------

=pod

=head1 NAME
	ProgressHistory - A module for storing, retrieving, and modifying student submission information.

=head1 SYNOPSIS

	use ProgressHistory;
	my $var = ProgressHistory->new();
	$var->method();

=head2 Methods

=over 4

=item C<new ( filename = "" )>

Create a new ProgressHistory object. Optional I<filename> automatically calls C<read_history( filename )>.

=item C<read_history( filename )> 

Open file I<filename> and copy the indicator history into memory.

=item C<write_history( filename )>

Open file I<filename> and write current indicator history to file.

=item C<add_submission>

Add a new submission to the current history. Any indicator values must be manually added with C<set_raw_indicator>.

=item C<set_raw_indicator( indicator_name, value, in_past = 0)>

Set the value of I<indicator_name> to I<value>. Optional I<in_past>: Set this value for the submission I<in_past> before the current submission.

=item C<get_raw_indicator( indicator_name )>

Return the raw indicator value for the current submission. Returns empty list if not set.

=item C<get_triggered_indicators>

Return a reference to a list of indicators that have triggered their threshold.

=item C<is_indicator_triggered( indicator_name )>

Comparing the current value for I<indicator_name> with the previous 4 valid values of I<indicator_name>, return whether it has passed its threshold.

=back

=head1 AUTHOR
Benjamin Murphy, Virginia Polytechnic Institute and State University <I<bmmurphy@vt.edu>>

=cut


