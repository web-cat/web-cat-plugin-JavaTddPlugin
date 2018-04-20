package Web_CAT::ExpandedFeedbackUtil;

use warnings;
use strict;
use Carp;
use File::stat;
use Web_CAT::Utilities qw(htmlEscape);
use Data::Dumper;
use Class::Struct;
use vars qw( @ISA @EXPORT_OK  );

use Exporter qw( import );

@ISA = qw( Exporter );

@EXPORT_OK = qw( extractAboveBelowLinesOfCode checkForPatternInFile
negateValueZeroToOneAndOneToZero extractLineOfCode);

#Always use long file name for "entityName"
struct expandedMessage =>
{
    entityName          =>      '$',
    lineNum             =>      '$',
    errorMessage        =>      '$',
    linesOfCode         =>      '$',
    enhancedMessage	=>	'$',
};

# PUBLIC METHODS

# Extract Lines of Code (Above and Below a line #number(including)
sub extractAboveBelowLinesOfCode
{
	my $filePath = shift;
	my $errorLineNum = shift;

    if (-e 'src/' . $filePath)
    {
        $filePath = 'src/' . $filePath;
    }
    if (! -e $filePath)
    {
        return '';
    }

	open(CODESUB, $filePath) || carp "could not read $filePath";

	my $lineNum = 0;
	my $returnCode = '';
	while (<CODESUB>)
	{
		$lineNum++;
		if ($lineNum > $errorLineNum + 1)
		{
			#print $returnCode;
			last;
		}

		if ($lineNum >= $errorLineNum - 1 && $lineNum <= $errorLineNum + 1)
		{
			$returnCode .= $lineNum . '. ' . $_;
		}
	}

	close CODESUB;

	return $returnCode;
}


# Extract a line of code-given line num
sub extractLineOfCode
{
	my $filePath = shift;
	my $errorLineNum = shift;

	open(CODESUB, $filePath) or carp "could not read $filePath";

	my $lineNum = 0;
	my $returnCode = '';
	while (<CODESUB>)
	{
		$lineNum++;

		if ($lineNum == $errorLineNum)
		{
			$returnCode .= $lineNum . '. ' . $_;
			last;
		}
	}

	close CODESUB;

	return $returnCode;
}


# Check if a pattern exists in a file or not.
# Return 1 if it exists.
sub checkForPatternInFile
{
	my $fileName = shift;
	my $pattern = shift;

	open FILECONTENT, $fileName or return 0;

	while (<FILECONTENT>)
	{
		if (index(lc($_),lc($pattern)) != -1)
		{
			return 1;
		}
	}

	close FILECONTENT;

	return 0;
}


sub negateValueZeroToOneAndOneToZero
{
    my $value = shift;

    if ($value == 1)
    {
        return 0;
    }
    else
    {
        return 1;
    }
}

1;
