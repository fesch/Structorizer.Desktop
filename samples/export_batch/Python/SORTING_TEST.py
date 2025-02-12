#!/usr/bin/python3
# -*- coding: utf-8 -*-
# SORTING_TEST_MAIN 
# generated by Structorizer 3.32-26 

# Copyright (C) 2019-10-02 Kay Gürtzig 
# License: GPLv3-link 
# GNU General Public License (V 3) 
# https://www.gnu.org/licenses/gpl.html 
# http://www.gnu.de/documents/gpl.de.html 

from enum import Enum

from threading import Thread
import math

# Implements the well-known BubbleSort algorithm. 
# Compares neigbouring elements and swaps them in case of an inversion. 
# Repeats this while inversions have been found. After every 
# loop passage at least one element (the largest one out of the 
# processed subrange) finds its final place at the end of the 
# subrange. 
def bubbleSort(values) :
    ende = length(values) - 2
    while True:
        # The index of the most recent swapping (-1 means no swapping done). 
        posSwapped = -1
        for i in range(0, ende+1, 1):
            if (values[i] > values[i+1]):
                temp = values[i]
                values[i] = values[i+1]
                values[i+1] = temp
                posSwapped = i

        ende = posSwapped - 1
        if posSwapped < 0:
            break

# Given a max-heap 'heap´ with element at index 'i´ possibly 
# violating the heap property wrt. its subtree upto and including 
# index range-1, restores heap property in the subtree at index i 
# again. 
def maxHeapify(heap, i, range) :
    # Indices of left and right child of node i 
    right = (i+1) * 2
    left = right - 1
    # Index of the (local) maximum 
    max = i
    if (left < range  and  heap[left] > heap[i]):
        max = left

    if (right < range  and  heap[right] > heap[max]):
        max = right

    if (max != i):
        temp = heap[i]
        heap[i] = heap[max]
        heap[max] = temp
        maxHeapify(heap, max, range)

# Partitions array 'values´ between indices 'start´ und 'stop´-1 with 
# respect to the pivot element initially at index 'p´ into smaller 
# and greater elements. 
# Returns the new (and final) index of the pivot element (which 
# separates the sequence of smaller elements from the sequence 
# of greater elements). 
# This is not the most efficient algorithm (about half the swapping 
# might still be avoided) but it is pretty clear. 
def partition(values, start, stop, p) :
    # Cache the pivot element 
    pivot = values[p]
    # Exchange the pivot element with the start element 
    values[p] = values[start]
    values[start] = pivot
    p = start
    # Beginning and end of the remaining undiscovered range 
    start = start + 1
    stop = stop - 1
    # Still unseen elements? 
    # Loop invariants: 
    # 1. p = start - 1 
    # 2. pivot = values[p] 
    # 3. i < start → values[i] ≤ pivot 
    # 4. stop < i → pivot < values[i] 
    while (start <= stop):
        # Fetch the first element of the undiscovered area 
        seen = values[start]
        # Does the checked element belong to the smaller area? 
        if (seen <= pivot):
            # Insert the seen element between smaller area and pivot element 
            values[p] = seen
            values[start] = pivot
            # Shift the border between lower and undicovered area, 
            # update pivot position. 
            p = p + 1
            start = start + 1
        else:
            # Insert the checked element between undiscovered and larger area 
            values[start] = values[stop]
            values[stop] = seen
            # Shift the border between undiscovered and larger area 
            stop = stop - 1

    return p

# Checks whether or not the passed-in array is (ascendingly) sorted. 
def testSorted(numbers) :
    isSorted = true
    i = 0
    # As we compare with the following element, we must stop at the penultimate index 
    while (isSorted  and  (i <= length(numbers)-2)):
        # Is there an inversion? 
        if (numbers[i] > numbers[i+1]):
            isSorted = false
        else:
            i = i + 1

    return isSorted

# Runs through the array heap and converts it to a max-heap 
# in a bottom-up manner, i.e. starts above the "leaf" level 
# (index >= length(heap) div 2) and goes then up towards 
# the root. 
def buildMaxHeap(heap) :
    lgth = length(heap)
    for k in range(lgth // 2 - 1, 0-1, -1):
        maxHeapify(heap, k, lgth)

# Recursively sorts a subrange of the given array 'values´.  
# start is the first index of the subsequence to be sorted, 
# stop is the index BEHIND the subsequence to be sorted. 
def quickSort(values, start, stop) :
    # At least 2 elements? (Less don't make sense.) 
    if (stop >= start + 2):
        # Select a pivot element, be p its index. 
        # (here: randomly chosen element out of start ... stop-1) 
        p = random(stop-start) + start
        # Partition the array into smaller and greater elements 
        # Get the resulting (and final) position of the pivot element 
        p = partition(values, start, stop, p)
        # Sort subsequances separately and independently ... 
        
        # ========================================================== 
        # ================= START PARALLEL SECTION ================= 
        # ========================================================== 
        thr1efee8e7_0 = Thread(target=quickSort, args=(values,start,p))
        thr1efee8e7_0.start()
        
        thr1efee8e7_1 = Thread(target=quickSort, args=(values,p+1,stop))
        thr1efee8e7_1.start()
        
        thr1efee8e7_0.join()
        thr1efee8e7_1.join()
        # ========================================================== 
        # ================== END PARALLEL SECTION ================== 
        # ========================================================== 
        

# Sorts the array 'values´ of numbers according to he heap sort 
# algorithm 
def heapSort(values) :
    buildMaxHeap(values)
    heapRange = length(values)
    for k in range(heapRange - 1, 1-1, -1):
        heapRange = heapRange - 1
        # Swap the maximum value (root of the heap) to the heap end 
        maximum = values[0]
        values[0] = values[heapRange]
        values[heapRange] = maximum
        maxHeapify(values, 0, heapRange)

# = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

# Creates three equal arrays of numbers and has them sorted with different sorting algorithms 
# to allow performance comparison via execution counting ("Collect Runtime Data" should 
# sensibly be switched on). 
# Requested input data are: Number of elements (size) and filing mode. 
while True:
    elementCount = input("elementCount")
    if elementCount >= 1:
        break

while True:
    modus = input("Filling: 1 = random, 2 = increasing, 3 = decreasing")
    if modus == 1  or  modus == 2  or  modus == 3:
        break

for i in range(0, elementCount-1+1, 1):
    if ((modus) == 1) :
        values1[i] = random(10000)
    elif ((modus) == 2) :
        values1[i] = i
    elif ((modus) == 3) :
        values1[i] = -i

# Copy the array for exact comparability 
for i in range(0, elementCount-1+1, 1):
    values2[i] = values1[i]
    values3[i] = values1[i]


# ========================================================== 
# ================= START PARALLEL SECTION ================= 
# ========================================================== 
thr58a90037_0 = Thread(target=bubbleSort, args=(values1,))
thr58a90037_0.start()

thr58a90037_1 = Thread(target=quickSort, args=(values2,0,elementCount))
thr58a90037_1.start()

thr58a90037_2 = Thread(target=heapSort, args=(values3,))
thr58a90037_2.start()

thr58a90037_0.join()
thr58a90037_1.join()
thr58a90037_2.join()
# ========================================================== 
# ================== END PARALLEL SECTION ================== 
# ========================================================== 

ok1 = testSorted(values1)
ok2 = testSorted(values2)
ok3 = testSorted(values3)
if (not  ok1  or   not  ok2  or   not  ok3):
    for i in range(0, elementCount-1+1, 1):
        if (values1[i] != values2[i]  or  values1[i] != values3[i]):
            print("Difference at [", i, "]: ", values1[i], " <-> ", values2[i], " <-> ", values3[i], sep='')

while True:
    show = input("Show arrays (yes/no)?")
    if show == "yes"  or  show == "no":
        break

if (show == "yes"):
    for i in range(0, elementCount - 1+1, 1):
        print("[", i, "]:\t", values1[i], "\t", values2[i], "\t", values3[i], sep='')

