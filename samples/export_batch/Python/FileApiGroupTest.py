# ======= 8< ======= FileApiGroupTest.py =============================== 

#!/usr/bin/python3
# -*- coding: utf-8 -*-
# readNumbers(fileName: string; numbers: array of integer; maxNumbers: integer): integer 
# generated by Structorizer 3.32-26 

# Copyright (C) 2020-03-21 Kay Gürtzig 
# License: GPLv3-link 
# GNU General Public License (V 3) 
# https://www.gnu.org/licenses/gpl.html 
# http://www.gnu.de/documents/gpl.de.html 

from enum import Enum
import math

#===== STRUCTORIZER FILE API START =====

class StructorizerFileAPI:
    'Facade class for the Structorizer File API, mapping it via class methods to Python constructs'
    
    _openFileTable = {}
    _nFiles = 0
    
    @classmethod
    def open(cls, filePath):
        fileNo = 0
        try:
            fileHandle = open(filePath, "r")
            cls._nFiles += 1
            fileNo = cls._nFiles
            cls._openFileTable[fileNo] = fileHandle
        except IOError:
            fileNo = -1
        return fileNo
    
    @classmethod
    def create(cls, filePath):
        fileNo = 0
        try:
            fileHandle = open(filePath, "w")
            cls._nFiles += 1
            fileNo = cls._nFiles
            cls._openFileTable[fileNo] = fileHandle
        except IOError:
            fileNo = -1
        return fileNo
    
    @classmethod
    def append(cls, filePath):
        fileNo = 0
        try:
            fileHandle = open(filePath, "a")
            cls._nFiles += 1
            fileNo = cls._nFiles
            cls._openFileTable[fileNo] = fileHandle
        except IOError:
            fileNo = -1
        return fileNo
    
    @classmethod
    def close(cls, fileNo):
        fileHandle = cls._openFileTable[fileNo]
        fileHandle.close()
        del cls._openFileTable[fileNo]
    
    @classmethod
    def isEOF(cls, fileNo):
        fileHandle = cls._openFileTable[fileNo]
        offset = fileHandle.tell()
        content = fileHandle.read(1)
        atEOF = content == ""
        fileHandle.seek(offset, 0)
        return atEOF
    
    @classmethod
    def write(cls, fileNo, value):
        fileHandle = cls._openFileTable[fileNo]
        fileHandle.write(str(value))
    
    @classmethod
    def writeLine(cls, fileNo, value):
        fileHandle = cls._openFileTable[fileNo]
        fileHandle.write(str(value)+"\n")
    
    @classmethod
    def _readWord(cls, fileHandle):
        word = ""
        char = fileHandle.read(1)
        if (char == ""):
            raise IOError("End of File")
        while (char != "" and char.isspace()):
            char = fileHandle.read(1)
        while (char != "" and not char.isspace()):
            word += char
            offset = fileHandle.tell()
            char = fileHandle.read(1)
            if (char.isspace()):
                fileHandle.seek(offset, 0)
        return word        
    
    @classmethod
    def read(cls, fileNo):
        fileHandle = cls._openFileTable[fileNo]
        word = cls._readWord(fileHandle)
        if (len(word) > 0 and (word[0] == '"' or word[0] == "'") and not(len(word) > 1 and word.endswith(word[0]))):
            nextWord = cls._readWord(fileHandle)
            if (len(word) > 0):
                word += " " + nextWord
            while (len(nextWord) > 0 and not(len(word) > 1 and word.endswith(word[0]))):
                nextWord = cls._readWord(fileHandle)
                if (len(word) > 0):
                    word += " " + nextWord
        elif (len(word) > 0 and word[0] == '{' and not(len(word) > 1 and word.endswith('}'))):
            nextWord = cls._readWord(fileHandle)
            if (len(word) > 0):
                word += " " + nextWord
            while (len(nextWord) > 0 and not(len(word) > 1 and word.endswith('}'))):
                nextWord = cls._readWord(fileHandle)
                if (len(word) > 0):
                    word += " " + nextWord
            if (word.endswith('}')):
                word = '[' + word[0:len(word)-1] + ']'
        try:
            value = eval(word)
        except:
            value = word
        return value
    
    @classmethod
    def readLine(cls, fileNo):
        fileHandle = cls._openFileTable[fileNo]
        line = ""
        char = fileHandle.read(1)
        if (char == ""):
            raise IOError("End of File")
        while (char != "" and char != '\n'):
            line += char
            char = fileHandle.read(1)
        return line
    
    @classmethod
    def readChar(cls, fileNo):
        fileHandle = cls._openFileTable[fileNo]
        return fileHandle.read(1)
    
    @classmethod
    def readInt(cls, fileNo):
        fileHandle = cls._openFileTable[fileNo]
        word = cls._readWord(fileHandle)
        return int(word)
    
    @classmethod
    def readDouble(cls, fileNo):
        fileHandle = cls._openFileTable[fileNo]
        word = cls._readWord(fileHandle)
        return float(word)

# Global functions as adapters for the StructorizerFileAPI methods
def fileOpen(filePath):
    return StructorizerFileAPI.open(filePath)

def fileCreate(filePath):
    return StructorizerFileAPI.create(filePath)

def fileAppend(filePath):
    return StructorizerFileAPI.append(filePath)

def fileClose(fileNo):
    StructorizerFileAPI.close(fileNo)
    return

def fileEOF(fileNo):
    return StructorizerFileAPI.isEOF(fileNo)

def fileWrite(fileNo, value):
    StructorizerFileAPI.write(fileNo, value)
    return

def fileWriteLine(fileNo, value):
    StructorizerFileAPI.writeLine(fileNo, value)
    return

def fileRead(fileNo):
    return StructorizerFileAPI.read(fileNo)

def fileReadChar(fileNo):
    return StructorizerFileAPI.readChar(fileNo)

def fileReadInt(fileNo):
    return StructorizerFileAPI.readInt(fileNo)

def fileReadDouble(fileNo):
    return StructorizerFileAPI.readDouble(fileNo)

def fileReadLine(fileNo):
    return StructorizerFileAPI.readLine(fileNo)
    
#===== STRUCTORIZER FILE API END =====

# Draws a bar chart from the array "values" of size nValues. 
# Turtleizer must be activated and will scale the chart into a square of 
# 500 x 500 pixels 
# Note: The function is not robust against empty array or totally equal values. 
def drawBarChart(values, nValues) :
    # Used range of the Turtleizer screen 
    xSize = 500
    ySize = 500
    kMin = 0
    kMax = 0
    for k in range(1, nValues-1+1, 1):
        if (values[k] > values[kMax]):
            kMax = k
        elif (values[k] < values[kMin]):
            kMin = k

    valMin = values[kMin]
    valMax = values[kMax]
    yScale = valMax * 1.0 / (ySize - 1)
    yAxis = ySize - 1
    if (valMin < 0):
        if (valMax > 0):
            yAxis = valMax * ySize * 1.0 / (valMax - valMin)
            yScale = (valMax - valMin) * 1.0 / (ySize - 1)
        else:
            yAxis = 1
            yScale = valMin * 1.0 / (ySize - 1)

    # draw coordinate axes 
    turtle.goto(1, ySize - 1)
    col1dd02175 = turtle.pencolor(); turtle.pencolor("#000000")
    turtle.forward(ySize -1)
    turtle.penup()
    turtle.backward(yAxis)
    turtle.right(90)
    turtle.pendown()
    turtle.forward(xSize -1)
    turtle.penup()
    turtle.backward(xSize-1)
    turtle.pencolor(col1dd02175)
    stripeWidth = xSize / nValues
    for k in range(0, nValues-1+1, 1):
        stripeHeight = values[k] * 1.0 / yScale
        if ((k % 3) == 0) :
            turtle.pencolor(255,0,0)
        elif ((k % 3) == 1) :
            turtle.pencolor(0, 255,0)
        elif ((k % 3) == 2) :
            turtle.pencolor(0, 0, 255)

        col31206beb = turtle.pencolor(); turtle.pencolor("#000000")
        turtle.fd(1)
        turtle.left(90)
        turtle.pendown()
        turtle.fd(stripeHeight)
        turtle.right(90)
        turtle.fd(stripeWidth - 1)
        turtle.right(90)
        turtle.forward(stripeHeight)
        turtle.left(90)
        turtle.penup()
        turtle.pencolor(col31206beb)

# = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

# Tries to read as many integer values as possible upto maxNumbers 
# from file fileName into the given array numbers. 
# Returns the number of the actually read numbers. May cause an exception. 
def readNumbers(fileName, numbers, maxNumbers) :
    nNumbers = 0
    fileNo = fileOpen(fileName)
    if (fileNo <= 0):
        raise Exception("File could not be opened!")

    try:
        while (not  fileEOF(fileNo)  and  nNumbers < maxNumbers):
            number = fileReadInt(fileNo)
            numbers[nNumbers] = number
            nNumbers = nNumbers + 1

    except Exception as error:
        raise 
    finally:
        fileClose(fileNo)
    
    return nNumbers

# ======= 8< =========================================================== 

#!/usr/bin/python3
# -*- coding: utf-8 -*-
# ComputeSum 
# generated by Structorizer 3.32-26 

# Copyright (C) 2020-03-21 Kay Gürtzig 
# License: GPLv3-link 
# GNU General Public License (V 3) 
# https://www.gnu.org/licenses/gpl.html 
# http://www.gnu.de/documents/gpl.de.html 

from enum import Enum
import FileApiGroupTest
import math
import turtle
turtle.colormode(255)
turtle.mode("logo")

# = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

# Computes the sum and average of the numbers read from a user-specified 
# text file (which might have been created via generateRandomNumberFile(4)). 
#  
# This program is part of an arrangement used to test group code export (issue 
# #828) with FileAPI dependency. 
# The input check loop has been disabled (replaced by a simple unchecked input 
# instruction) in order to test the effect of indirect FileAPI dependency (only the 
# called subroutine directly requires FileAPI now). 
fileNo = 1000
# Disable this if you enable the loop below! 
file_name = input("Name/path of the number file")
# If you enable this loop, then the preceding input instruction is to be disabled 
# and the fileClose instruction in the alternative below is to be enabled. 
# while True: 
#     file_name = input("Name/path of the number file") 
#     fileNo = fileOpen(file_name) 
#     if fileNo > 0  or  file_name == "": 
#         break 
#  
if (fileNo > 0):
    # This should be enabled if the input check loop above gets enabled. 
#     fileClose(fileNo) 
    values = []
    nValues = 0
    try:
        nValues = readNumbers(file_name, values, 1000)
    except Exception as failure:
        print(failure, sep='')
        # FIXME: unsupported jump/exit instruction! 
        # exit -7 
    
    sum = 0.0
    for k in range(0, nValues-1+1, 1):
        sum = sum + values[k]

    print("sum = ", sum, sep='')
    print("average = ", sum / nValues, sep='')

# turtle.bye()	# TODO: re-enable this if you want to close the turtle window. 

# ======= 8< =========================================================== 

#!/usr/bin/python3
# -*- coding: utf-8 -*-
# DrawRandomHistogram 
# generated by Structorizer 3.32-26 

# Copyright (C) 2020-03-21 Kay Gürtzig 
# License: GPLv3-link 
# GNU General Public License (V 3) 
# https://www.gnu.org/licenses/gpl.html 
# http://www.gnu.de/documents/gpl.de.html 

from enum import Enum
import FileApiGroupTest
import math
import turtle
turtle.colormode(255)
turtle.mode("logo")

#===== STRUCTORIZER FILE API START =====

class StructorizerFileAPI:
    'Facade class for the Structorizer File API, mapping it via class methods to Python constructs'
    
    _openFileTable = {}
    _nFiles = 0
    
    @classmethod
    def open(cls, filePath):
        fileNo = 0
        try:
            fileHandle = open(filePath, "r")
            cls._nFiles += 1
            fileNo = cls._nFiles
            cls._openFileTable[fileNo] = fileHandle
        except IOError:
            fileNo = -1
        return fileNo
    
    @classmethod
    def create(cls, filePath):
        fileNo = 0
        try:
            fileHandle = open(filePath, "w")
            cls._nFiles += 1
            fileNo = cls._nFiles
            cls._openFileTable[fileNo] = fileHandle
        except IOError:
            fileNo = -1
        return fileNo
    
    @classmethod
    def append(cls, filePath):
        fileNo = 0
        try:
            fileHandle = open(filePath, "a")
            cls._nFiles += 1
            fileNo = cls._nFiles
            cls._openFileTable[fileNo] = fileHandle
        except IOError:
            fileNo = -1
        return fileNo
    
    @classmethod
    def close(cls, fileNo):
        fileHandle = cls._openFileTable[fileNo]
        fileHandle.close()
        del cls._openFileTable[fileNo]
    
    @classmethod
    def isEOF(cls, fileNo):
        fileHandle = cls._openFileTable[fileNo]
        offset = fileHandle.tell()
        content = fileHandle.read(1)
        atEOF = content == ""
        fileHandle.seek(offset, 0)
        return atEOF
    
    @classmethod
    def write(cls, fileNo, value):
        fileHandle = cls._openFileTable[fileNo]
        fileHandle.write(str(value))
    
    @classmethod
    def writeLine(cls, fileNo, value):
        fileHandle = cls._openFileTable[fileNo]
        fileHandle.write(str(value)+"\n")
    
    @classmethod
    def _readWord(cls, fileHandle):
        word = ""
        char = fileHandle.read(1)
        if (char == ""):
            raise IOError("End of File")
        while (char != "" and char.isspace()):
            char = fileHandle.read(1)
        while (char != "" and not char.isspace()):
            word += char
            offset = fileHandle.tell()
            char = fileHandle.read(1)
            if (char.isspace()):
                fileHandle.seek(offset, 0)
        return word        
    
    @classmethod
    def read(cls, fileNo):
        fileHandle = cls._openFileTable[fileNo]
        word = cls._readWord(fileHandle)
        if (len(word) > 0 and (word[0] == '"' or word[0] == "'") and not(len(word) > 1 and word.endswith(word[0]))):
            nextWord = cls._readWord(fileHandle)
            if (len(word) > 0):
                word += " " + nextWord
            while (len(nextWord) > 0 and not(len(word) > 1 and word.endswith(word[0]))):
                nextWord = cls._readWord(fileHandle)
                if (len(word) > 0):
                    word += " " + nextWord
        elif (len(word) > 0 and word[0] == '{' and not(len(word) > 1 and word.endswith('}'))):
            nextWord = cls._readWord(fileHandle)
            if (len(word) > 0):
                word += " " + nextWord
            while (len(nextWord) > 0 and not(len(word) > 1 and word.endswith('}'))):
                nextWord = cls._readWord(fileHandle)
                if (len(word) > 0):
                    word += " " + nextWord
            if (word.endswith('}')):
                word = '[' + word[0:len(word)-1] + ']'
        try:
            value = eval(word)
        except:
            value = word
        return value
    
    @classmethod
    def readLine(cls, fileNo):
        fileHandle = cls._openFileTable[fileNo]
        line = ""
        char = fileHandle.read(1)
        if (char == ""):
            raise IOError("End of File")
        while (char != "" and char != '\n'):
            line += char
            char = fileHandle.read(1)
        return line
    
    @classmethod
    def readChar(cls, fileNo):
        fileHandle = cls._openFileTable[fileNo]
        return fileHandle.read(1)
    
    @classmethod
    def readInt(cls, fileNo):
        fileHandle = cls._openFileTable[fileNo]
        word = cls._readWord(fileHandle)
        return int(word)
    
    @classmethod
    def readDouble(cls, fileNo):
        fileHandle = cls._openFileTable[fileNo]
        word = cls._readWord(fileHandle)
        return float(word)

# Global functions as adapters for the StructorizerFileAPI methods
def fileOpen(filePath):
    return StructorizerFileAPI.open(filePath)

def fileCreate(filePath):
    return StructorizerFileAPI.create(filePath)

def fileAppend(filePath):
    return StructorizerFileAPI.append(filePath)

def fileClose(fileNo):
    StructorizerFileAPI.close(fileNo)
    return

def fileEOF(fileNo):
    return StructorizerFileAPI.isEOF(fileNo)

def fileWrite(fileNo, value):
    StructorizerFileAPI.write(fileNo, value)
    return

def fileWriteLine(fileNo, value):
    StructorizerFileAPI.writeLine(fileNo, value)
    return

def fileRead(fileNo):
    return StructorizerFileAPI.read(fileNo)

def fileReadChar(fileNo):
    return StructorizerFileAPI.readChar(fileNo)

def fileReadInt(fileNo):
    return StructorizerFileAPI.readInt(fileNo)

def fileReadDouble(fileNo):
    return StructorizerFileAPI.readDouble(fileNo)

def fileReadLine(fileNo):
    return StructorizerFileAPI.readLine(fileNo)
    
#===== STRUCTORIZER FILE API END =====

# Draws a bar chart from the array "values" of size nValues. 
# Turtleizer must be activated and will scale the chart into a square of 
# 500 x 500 pixels 
# Note: The function is not robust against empty array or totally equal values. 
def drawBarChart(values, nValues) :
    # Used range of the Turtleizer screen 
    xSize = 500
    ySize = 500
    kMin = 0
    kMax = 0
    for k in range(1, nValues-1+1, 1):
        if (values[k] > values[kMax]):
            kMax = k
        elif (values[k] < values[kMin]):
            kMin = k

    valMin = values[kMin]
    valMax = values[kMax]
    yScale = valMax * 1.0 / (ySize - 1)
    yAxis = ySize - 1
    if (valMin < 0):
        if (valMax > 0):
            yAxis = valMax * ySize * 1.0 / (valMax - valMin)
            yScale = (valMax - valMin) * 1.0 / (ySize - 1)
        else:
            yAxis = 1
            yScale = valMin * 1.0 / (ySize - 1)

    # draw coordinate axes 
    turtle.goto(1, ySize - 1)
    col1dd02175 = turtle.pencolor(); turtle.pencolor("#000000")
    turtle.forward(ySize -1)
    turtle.penup()
    turtle.backward(yAxis)
    turtle.right(90)
    turtle.pendown()
    turtle.forward(xSize -1)
    turtle.penup()
    turtle.backward(xSize-1)
    turtle.pencolor(col1dd02175)
    stripeWidth = xSize / nValues
    for k in range(0, nValues-1+1, 1):
        stripeHeight = values[k] * 1.0 / yScale
        if ((k % 3) == 0) :
            turtle.pencolor(255,0,0)
        elif ((k % 3) == 1) :
            turtle.pencolor(0, 255,0)
        elif ((k % 3) == 2) :
            turtle.pencolor(0, 0, 255)

        col31206beb = turtle.pencolor(); turtle.pencolor("#000000")
        turtle.fd(1)
        turtle.left(90)
        turtle.pendown()
        turtle.fd(stripeHeight)
        turtle.right(90)
        turtle.fd(stripeWidth - 1)
        turtle.right(90)
        turtle.forward(stripeHeight)
        turtle.left(90)
        turtle.penup()
        turtle.pencolor(col31206beb)

# = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

# Reads a random number file and draws a histogram accotrding to the 
# user specifications 
fileNo = -10
while True:
    file_name = input("Name/path of the number file")
    fileNo = fileOpen(file_name)
    if fileNo > 0  or  file_name == "":
        break

if (fileNo > 0):
    fileClose(fileNo)
    nIntervals = input("number of intervals")
    # Initialize the interval counters 
    for k in range(0, nIntervals-1+1, 1):
        count[k] = 0

    # Index of the most populated interval 
    kMaxCount = 0
    numberArray = []
    nObtained = 0
    try:
        nObtained = readNumbers(file_name, numberArray, 10000)
    except Exception as failure:
        print(failure, sep='')
    
    if (nObtained > 0):
        min = numberArray[0]
        max = numberArray[0]
        for i in range(1, nObtained-1+1, 1):
            if (numberArray[i] < min):
                min = numberArray[i]
            elif (numberArray[i] > max):
                max = numberArray[i]

        # Interval width 
        width = (max - min) * 1.0 / nIntervals
        for i in range(0, nObtained - 1+1, 1):
            value = numberArray[i]
            k = 1
            while (k < nIntervals  and  value > min + k * width):
                k = k + 1

            count[k-1] = count[k-1] + 1
            if (count[k-1] > count[kMaxCount]):
                kMaxCount = k-1

        drawBarChart(count, nIntervals)
        print("Interval with max count: ", kMaxCount, " (", count[kMaxCount], ")", sep='')
        for k in range(0, nIntervals-1+1, 1):
            print(count[k], " numbers in interval ", k, " (", min + k * width, " ... ", min + (k+1) * width, ")", sep='')

    else:
        print("No numbers read.", sep='')

# turtle.bye()	# TODO: re-enable this if you want to close the turtle window. 
