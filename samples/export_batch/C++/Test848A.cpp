// Generated by Structorizer 3.32-26 

// Copyright (C) 2020-04-13 Kay Gürtzig 
// License: GPLv3-link 
// GNU General Public License (V 3) 
// https://www.gnu.org/licenses/gpl.html 
// http://www.gnu.de/documents/gpl.de.html 

#include <string>
#include <iostream>
using std::string;

string a2;
int a1;

double b2;
string b1;

// function initialize_IncludeA() 

// Automatically created initialization procedure for IncludeA 
// TODO: Revise the return type and declare the parameters. 
void initialize_IncludeA()
{
	static bool initDone_IncludeA = false;
	if (! initDone_IncludeA) {
		a2 = "doof";
		initDone_IncludeA = true;
	}
}

// function initialize_IncludeB() 

// Automatically created initialization procedure for IncludeB 
// TODO: Revise the return type and declare the parameters. 
void initialize_IncludeB()
{
	static bool initDone_IncludeB = false;
	if (! initDone_IncludeB) {
		b2 = 3.9;
		initDone_IncludeB = true;
	}
}

// function testSub(c1, param2: real; b1: string): int 

// TODO: Revise the return type and declare the parameters. 
int testSub(double c1, double param2, string b1)
{
	// TODO: Check and accomplish variable declarations: 
	string d2;
	int d1;

	initialize_IncludeA();
	
	d1 = a1;
	d2 = a2;
	a1 = 15;
	return length(a2 + b1) + floor(c1) * ceil(param2);
}
// = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

// program TestMain 

// Test for indirect and concurrent includes 
int main(void)
{
	// TODO: Check and accomplish variable declarations: 
	int c2;

	initialize_IncludeA();
	initialize_IncludeB();
	
	a1 = 7;
	b1 = a2 + " oder blöd";
	??? c1 = a1 * b2;
	c2 = testSub(c1, a1 + b2, b1);
	std::cout << a1 << " " << a2 << " " << b1 << " " << b2 << std::endl;

	return 0;
}
