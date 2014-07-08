#!/usr/bin/perl
#=============================================================================
#   @(#)$Id$
#-----------------------------------------------------------------------------
#   usage:
#       ensureUTF8.pl <file1> ...
#
#
#=============================================================================

use strict;

use Web_CAT::Utilities qw(loadFileAsUtf8);

for my $file (@ARGV)
{
    # Capture file attributes
    my @stat = stat($file);

    # Read in file contents
    my @lines = loadFileAsUtf8($file);

    # Rewrite file
    open(OUT, ">:encoding(UTF-8)", $file)
        or die "Cannot open $file for writing: $!";
    print OUT @lines;
    close(OUT);
    utime $stat[8], $stat[9], $file;
}
