<script>
// program TestMain 
// Generated by Structorizer 3.32-26 

// Copyright (C) 2020-04-13 Kay Gürtzig 
// License: GPLv3-link 
// GNU General Public License (V 3) 
// https://www.gnu.org/licenses/gpl.html 
// http://www.gnu.de/documents/gpl.de.html 

var initDone_IncludeA = false;
var a2;
var a1;

var initDone_IncludeB = false;
var b2;
var b1;

// function initialize_IncludeA() 
// Automatically created initialization procedure for IncludeA 
function initialize_IncludeA() {
	if (! initDone_IncludeA) {
		a2 = "doof";
		initDone_IncludeA = true;
	}
}

// function initialize_IncludeB() 
// Automatically created initialization procedure for IncludeB 
function initialize_IncludeB() {
	if (! initDone_IncludeB) {
		b2 = 3.9;
		initDone_IncludeB = true;
	}
}

// function testSub(c1, param2: real; b1: string): int 
function testSub(c1, param2, b1) {
	initialize_IncludeA();
	
	var d2;
	var d1;

	d1 = a1;
	d2 = a2;
	a1 = 15;
	return length(a2 + b1) + floor(c1) * ceil(param2);
}
// = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

// Test for indirect and concurrent includes 
initialize_IncludeA();
initialize_IncludeB();

var c2;
var c1;

a1 = 7;
b1 = a2 + " oder blöd";
c1 = a1 * b2;
c2 = testSub(c1, a1 + b2, b1);
document.write((a1, " ", a2, " ", b1, " ", b2) + "<br/>");
</script>
