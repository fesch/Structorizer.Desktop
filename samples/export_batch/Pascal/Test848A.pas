{ Test for indirect and concurrent includes }
program TestMain;
{ Generated by Structorizer 3.32-26 }

{ Copyright (C) 2020-04-13 Kay Gürtzig }
{ License: GPLv3-link }
{
  GNU General Public License (V 3)
  https://www.gnu.org/licenses/gpl.html
  http://www.gnu.de/documents/gpl.de.html
}

var
  a2: String;
  b2: Double;
  c2: Longint;
  c1: ???;	{ FIXME! }
  b1: string;
  a1: Longint;

function testSub(c1: Single; param2: Single; b1: string): Longint;

var
  d2: String;
  d1: Longint;

begin
  d1 := a1;
  d2 := a2;
  a1 := 15;
  testSub := length(a2 + b1) + floor(c1) * ceil(param2);

end;

{ = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = }


begin
  a2 := 'doof';
  b2 := 3.9;
  a1 := 7;
  b1 := a2 + ' oder blöd';
  c1 := a1 * b2;
  c2 := testSub(c1, a1 + b2, b1);
  writeln(a1, ' ', a2, ' ', b1, ' ', b2);
end.
