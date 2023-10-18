// Generated by Structorizer 3.32-13 

// Copyright (C) 2019-10-02 Kay Gürtzig 
// License: GPLv3-link 
// GNU General Public License (V 3) 
// https://www.gnu.org/licenses/gpl.html 
// http://www.gnu.de/documents/gpl.de.html 

#include <string>
#include <iostream>
#include <thread>
using std::string;

// function bubbleSort(values) 

// Implements the well-known BubbleSort algorithm. 
// Compares neigbouring elements and swaps them in case of an inversion. 
// Repeats this while inversions have been found. After every 
// loop passage at least one element (the largest one out of the 
// processed subrange) finds its final place at the end of the 
// subrange. 
// TODO: Revise the return type and declare the parameters. 
void bubbleSort(??? values)
{
	// TODO: Check and accomplish variable declarations: 
	int posSwapped;

	??? ende = length(values) - 2;
	do {
		// The index of the most recent swapping (-1 means no swapping done). 
		posSwapped = -1;
		for (int i = 0; i <= ende; i += (1)) {
			if (values[i] > values[i+1]) {
				??? temp = values[i];
				values[i] = values[i+1];
				values[i+1] = temp;
				posSwapped = i;
			}
		}
		ende = posSwapped - 1;
	} while (! (posSwapped < 0));
}

// function maxHeapify(heap, i, range) 

// Given a max-heap 'heap´ with element at index 'i´ possibly 
// violating the heap property wrt. its subtree upto and including 
// index range-1, restores heap property in the subtree at index i 
// again. 
// TODO: Revise the return type and declare the parameters. 
void maxHeapify(??? heap, ??? i, ??? range)
{
	// TODO: Check and accomplish variable declarations: 

	// Indices of left and right child of node i 
	??? right = (i+1) * 2;
	??? left = right - 1;
	// Index of the (local) maximum 
	??? max = i;
	if (left < range && heap[left] > heap[i]) {
		max = left;
	}
	if (right < range && heap[right] > heap[max]) {
		max = right;
	}
	if (max != i) {
		??? temp = heap[i];
		heap[i] = heap[max];
		heap[max] = temp;
		maxHeapify(heap, max, range);
	}
}

// function partition(values, start, stop, p): int 

// Partitions array 'values´ between indices 'start´ und 'stop´-1 with 
// respect to the pivot element initially at index 'p´ into smaller 
// and greater elements. 
// Returns the new (and final) index of the pivot element (which 
// separates the sequence of smaller elements from the sequence 
// of greater elements). 
// This is not the most efficient algorithm (about half the swapping 
// might still be avoided) but it is pretty clear. 
// TODO: Revise the return type and declare the parameters. 
int partition(??? values, ??? start, ??? stop, ??? p)
{
	// TODO: Check and accomplish variable declarations: 

	// Cache the pivot element 
	??? pivot = values[p];
	// Exchange the pivot element with the start element 
	values[p] = values[start];
	values[start] = pivot;
	p = start;
	// Beginning and end of the remaining undiscovered range 
	start = start + 1;
	stop = stop - 1;
	// Still unseen elements? 
	// Loop invariants: 
	// 1. p = start - 1 
	// 2. pivot = values[p] 
	// 3. i < start → values[i] ≤ pivot 
	// 4. stop < i → pivot < values[i] 
	while (start <= stop) {
		// Fetch the first element of the undiscovered area 
		??? seen = values[start];
		// Does the checked element belong to the smaller area? 
		if (seen <= pivot) {
			// Insert the seen element between smaller area and pivot element 
			values[p] = seen;
			values[start] = pivot;
			// Shift the border between lower and undicovered area, 
			// update pivot position. 
			p = p + 1;
			start = start + 1;
		}
		else {
			// Insert the checked element between undiscovered and larger area 
			values[start] = values[stop];
			values[stop] = seen;
			// Shift the border between undiscovered and larger area 
			stop = stop - 1;
		}
	}
	return p;
}

// function testSorted(numbers): bool 

// Checks whether or not the passed-in array is (ascendingly) sorted. 
// TODO: Revise the return type and declare the parameters. 
bool testSorted(??? numbers)
{
	// TODO: Check and accomplish variable declarations: 
	bool isSorted;
	int i;

	isSorted = true;
	i = 0;
	// As we compare with the following element, we must stop at the penultimate index 
	while (isSorted && (i <= length(numbers)-2)) {
		// Is there an inversion? 
		if (numbers[i] > numbers[i+1]) {
			isSorted = false;
		}
		else {
			i = i + 1;
		}
	}
	return isSorted;
}

// function buildMaxHeap(heap) 

// Runs through the array heap and converts it to a max-heap 
// in a bottom-up manner, i.e. starts above the "leaf" level 
// (index >= length(heap) div 2) and goes then up towards 
// the root. 
// TODO: Revise the return type and declare the parameters. 
void buildMaxHeap(??? heap)
{
	// TODO: Check and accomplish variable declarations: 
	int lgth;

	lgth = length(heap);
	for (int k = lgth / 2 - 1; k >= 0; k += (-1)) {
		maxHeapify(heap, k, lgth);
	}
}

// function quickSort(values, start, stop) 

// Recursively sorts a subrange of the given array 'values´.  
// start is the first index of the subsequence to be sorted, 
// stop is the index BEHIND the subsequence to be sorted. 
// TODO: Revise the return type and declare the parameters. 
void quickSort(??? values, ??? start, ??? stop)
{
	class ThrFuncc818ce5a_0{
	public:
		void operator()(???& values, ???& start, ???& p) {
			// Sort left (lower) array part 
			quickSort(values, start, p);
		}
	};

	class ThrFuncc818ce5a_1{
	public:
		void operator()(???& values, ???& p, ???& stop) {
			// Sort right (higher) array part 
			quickSort(values, p+1, stop);
		}
	};

	// TODO: Check and accomplish variable declarations: 

	// At least 2 elements? (Less don't make sense.) 
	if (stop >= start + 2) {
		// Select a pivot element, be p its index. 
		// (here: randomly chosen element out of start ... stop-1) 
		??? p = random(stop-start) + start;
		// Partition the array into smaller and greater elements 
		// Get the resulting (and final) position of the pivot element 
		p = partition(values, start, stop, p);
		// Sort subsequances separately and independently ... 

		// Parallel section 
		{
			ThrFuncc818ce5a_0 thrfuncc818ce5a_0;
			std::thread thrc818ce5a_0(thrfuncc818ce5a_0, values, start, p);
		
			ThrFuncc818ce5a_1 thrfuncc818ce5a_1;
			std::thread thrc818ce5a_1(thrfuncc818ce5a_1, values, p, stop);
		
			thrc818ce5a_0.join();
			thrc818ce5a_1.join();
		}

	}
}

// function heapSort(values) 

// Sorts the array 'values´ of numbers according to he heap sort 
// algorithm 
// TODO: Revise the return type and declare the parameters. 
void heapSort(??? values)
{
	// TODO: Check and accomplish variable declarations: 
	int heapRange;

	buildMaxHeap(values);
	heapRange = length(values);
	for (int k = heapRange - 1; k >= 1; k += (-1)) {
		heapRange = heapRange - 1;
		// Swap the maximum value (root of the heap) to the heap end 
		??? maximum = values[0];
		values[0] = values[heapRange];
		values[heapRange] = maximum;
		maxHeapify(values, 0, heapRange);
	}
}
// = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

// program SORTING_TEST_MAIN 

// Creates three equal arrays of numbers and has them sorted with different sorting algorithms 
// to allow performance comparison via execution counting ("Collect Runtime Data" should 
// sensibly be switched on). 
// Requested input data are: Number of elements (size) and filing mode. 
int main(void)
{
	class ThrFunc6a681ec8_0{
	public:
		void operator()(int values1[50]) {
			bubbleSort(values1);
		}
	};

	class ThrFunc6a681ec8_1{
	public:
		void operator()(int values2[50], ???& elementCount) {
			quickSort(values2, 0, elementCount);
		}
	};

	class ThrFunc6a681ec8_2{
	public:
		void operator()(int values3[50]) {
			heapSort(values3);
		}
	};

	// TODO: Check and accomplish variable declarations: 
	int values3[50];
	int values2[50];
	int values1[50];
	??? show;
	bool ok3;
	bool ok2;
	bool ok1;
	??? modus;
	??? elementCount;

	do {
		std::cin >> elementCount;
	} while (! (elementCount >= 1));
	do {
		std::cout << "Filling: 1 = random, 2 = increasing, 3 = decreasing"; std::cin >> modus;
	} while (! (modus == 1 || modus == 2 || modus == 3));
	for (int i = 0; i <= elementCount-1; i += (1)) {
		switch (modus) {
		case 1:
			values1[i] = random(10000);
			break;
		case 2:
			values1[i] = i;
			break;
		case 3:
			values1[i] = -i;
			break;
		}
	}
	// Copy the array for exact comparability 
	for (int i = 0; i <= elementCount-1; i += (1)) {
		values2[i] = values1[i];
		values3[i] = values1[i];
	}

	// Parallel section 
	{
		ThrFunc6a681ec8_0 thrfunc6a681ec8_0;
		std::thread thr6a681ec8_0(thrfunc6a681ec8_0, values1);
	
		ThrFunc6a681ec8_1 thrfunc6a681ec8_1;
		std::thread thr6a681ec8_1(thrfunc6a681ec8_1, values2, elementCount);
	
		ThrFunc6a681ec8_2 thrfunc6a681ec8_2;
		std::thread thr6a681ec8_2(thrfunc6a681ec8_2, values3);
	
		thr6a681ec8_0.join();
		thr6a681ec8_1.join();
		thr6a681ec8_2.join();
	}

	ok1 = testSorted(values1);
	ok2 = testSorted(values2);
	ok3 = testSorted(values3);
	if (! ok1 || ! ok2 || ! ok3) {
		for (int i = 0; i <= elementCount-1; i += (1)) {
			if (values1[i] != values2[i] || values1[i] != values3[i]) {
				std::cout << "Difference at [" << i << "]: " << values1[i] << " <-> " << values2[i] << " <-> " << values3[i] << std::endl;
			}
		}
	}
	do {
		std::cout << "Show arrays (yes/no)?"; std::cin >> show;
	} while (! (show == "yes" || show == "no"));
	if (show == "yes") {
		for (int i = 0; i <= elementCount - 1; i += (1)) {
			std::cout << "[" << i << "]:\t" << values1[i] << "\t" << values2[i] << "\t" << values3[i] << std::endl;
		}
	}

	return 0;
}
