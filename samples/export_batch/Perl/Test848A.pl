#!/usr/bin/perl
# Generated by Structorizer 3.32-25 

# Copyright (C) 2020-04-13 Kay Gürtzig 
# License: GPLv3-link 
# GNU General Public License (V 3) 
# https://www.gnu.org/licenses/gpl.html 
# http://www.gnu.de/documents/gpl.de.html 

use strict;
use warnings;
use Class::Struct;

sub testSub {
    my $c1 = $_[0];
    my $param2 = $_[1];
    my $b1 = $_[2];

    my $d2;
    my $d1;
    my $a2;
    my $a1;

    $d1 = $a1;
    $d2 = $a2;
    $a1 = 15;
    return length($a2 + $b1) + floor($c1) * ceil($param2);
}

# = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

# Test for indirect and concurrent includes 

my $c2;
my $c1;
my $b2;
my $b1;
my $a2;
my $a1;

$a1 = 7;
$b1 = $a2 + " oder blöd";
$c1 = $a1 * $b2;
$c2 = testSub($c1, $a1 + $b2, $b1);
print $a1, " ", $a2, " ", $b1, " ", $b2, "\n";
