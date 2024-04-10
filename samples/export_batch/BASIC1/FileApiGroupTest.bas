Rem Tries to read as many integer values as possible upto maxNumbers 
Rem from file fileName into the given array numbers. 
Rem Returns the number of the actually read numbers. May cause an exception. 
Rem Generated by Structorizer 3.32-20 

Rem Copyright (C) 2020-03-21 Kay Gürtzig 
Rem License: GPLv3-link 
Rem GNU General Public License (V 3) 
Rem https://www.gnu.org/licenses/gpl.html 
Rem http://www.gnu.de/documents/gpl.de.html 

Rem  
Rem TODO: Check (and specify if needed) the argument and result types! 
Public Function readNumbers(fileName As String, numbers As integer(50), maxNumbers As integer) As integer
  Rem TODO: Check and accomplish your variable declarations here: 
  Dim number As Integer
  Dim nNumbers As Integer
  Dim fileNo As Integer
  Rem  
  nNumbers = 0
  fileNo = fileOpen(fileName)
  If fileNo <= 0 Then
    Throw New Exception("File could not be opened!")
  End If
  Try
    Do While NOT fileEOF(fileNo) AND nNumbers < maxNumbers
      number = fileReadInt(fileNo)
      numbers(nNumbers) = number
      nNumbers = nNumbers + 1
    Loop
  Catch ex4eb7f003 As Exception
    Dim error As String = ex4eb7f003.ToString()
    Throw 
  Finally
    fileClose(fileNo)
  End Try
  Return nNumbers
End Function
Rem  
Rem Draws a bar chart from the array "values" of size nValues. 
Rem Turtleizer must be activated and will scale the chart into a square of 
Rem 500 x 500 pixels 
Rem Note: The function is not robust against empty array or totally equal values. 
Rem TODO: Check (and specify if needed) the argument and result types! 
Sub drawBarChart(values As double(50), nValues)
  Rem TODO: Check and accomplish your variable declarations here: 
  Dim ySize As Integer
  Dim yScale As ???
  Dim yAxis As ???
  Dim xSize As Integer
  Dim valMin As double
  Dim valMax As double
  Dim stripeWidth As ???
  Dim stripeHeight As ???
  Dim kMin As Integer
  Dim kMax As Integer
  Dim k As Integer
  Rem  
  Rem Used range of the Turtleizer screen 
  Const xSize = 500
  Const ySize = 500
  kMin = 0
  kMax = 0
  For k = 1 To nValues-1
    If values(k) > values(kMax) Then
      kMax = k
    Elseif values(k) < values(kMin) Then
      kMin = k
    End If
  Next k
  valMin = values(kMin)
  valMax = values(kMax)
  yScale = valMax * 1.0 / (ySize - 1)
  yAxis = ySize - 1
  If valMin < 0 Then
    If valMax > 0 Then
      yAxis = valMax * ySize * 1.0 / (valMax - valMin)
      yScale = (valMax - valMin) * 1.0 / (ySize - 1)
    Else
      yAxis = 1
      yScale = valMin * 1.0 / (ySize - 1)
    End If
  End If
  Rem draw coordinate axes 
  gotoXY(1, ySize - 1)
  forward(ySize -1) : Rem color = ffffff
  penUp()
  backward(yAxis) : Rem color = ffffff
  right(90)
  penDown()
  forward(xSize -1) : Rem color = ffffff
  penUp()
  backward(xSize-1) : Rem color = ffffff
  stripeWidth = xSize / nValues
  For k = 0 To nValues-1
    stripeHeight = values(k) * 1.0 / yScale
    Select Case k % 3
      Case 0
        setPenColor(255,0,0)
      Case 1
        setPenColor(0, 255,0)
      Case 2
        setPenColor(0, 0, 255)
    End Select
    fd(1) : Rem color = ffffff
    left(90)
    penDown()
    fd(stripeHeight) : Rem color = ffffff
    right(90)
    fd(stripeWidth - 1) : Rem color = ffffff
    right(90)
    forward(stripeHeight) : Rem color = ffffff
    left(90)
    penUp()
  Next k
End Sub

Rem = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

Rem ======= 8< =========================================================== 

Rem Computes the sum and average of the numbers read from a user-specified 
Rem text file (which might have been created via generateRandomNumberFile(4)). 
Rem  
Rem This program is part of an arrangement used to test group code export (issue 
Rem #828) with FileAPI dependency. 
Rem The input check loop has been disabled (replaced by a simple unchecked input 
Rem instruction) in order to test the effect of indirect FileAPI dependency (only the 
Rem called subroutine directly requires FileAPI now). 
Rem Generated by Structorizer 3.32-20 

Rem Copyright (C) 2020-03-21 Kay Gürtzig 
Rem License: GPLv3-link 
Rem GNU General Public License (V 3) 
Rem https://www.gnu.org/licenses/gpl.html 
Rem http://www.gnu.de/documents/gpl.de.html 

Rem  
Rem program ComputeSum
Rem TODO: Check and accomplish your variable declarations here: 
Dim values() As ???
Dim sum As double
Dim nValues As Integer
Dim k As Integer
Dim file_name As ???
Dim fileNo As Integer
Rem  
fileNo = 1000
Rem Disable this if you enable the loop below! 
PRINT "Name/path of the number file"; : INPUT file_name
Rem If you enable this loop, then the preceding input instruction is to be disabled 
Rem and the fileClose instruction in the alternative below is to be enabled. 
Rem Do 
Rem   PRINT "Name/path of the number file"; : INPUT file_name 
Rem   fileNo = fileOpen(file_name) 
Rem Loop Until fileNo > 0 OR file_name = "" 
If fileNo > 0 Then
  Rem This should be enabled if the input check loop above gets enabled. 
Rem   fileClose(fileNo) 
  Let values = Array()
  nValues = 0
  Try
    nValues = readNumbers(file_name, values, 1000)
  Catch ex4a87761d As Exception
    Dim failure As String = ex4a87761d.ToString()
    PRINT failure
    Stop
  End Try
  sum = 0.0
  For k = 0 To nValues-1
    sum = sum + values(k)
  Next k
  PRINT "sum = "; sum
  PRINT "average = "; sum / nValues
End If
End

Rem = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

Rem ======= 8< =========================================================== 

Rem Reads a random number file and draws a histogram accotrding to the 
Rem user specifications 
Rem Generated by Structorizer 3.32-20 

Rem Copyright (C) 2020-03-21 Kay Gürtzig 
Rem License: GPLv3-link 
Rem GNU General Public License (V 3) 
Rem https://www.gnu.org/licenses/gpl.html 
Rem http://www.gnu.de/documents/gpl.de.html 

Rem  
Rem program DrawRandomHistogram
Rem TODO: Check and accomplish your variable declarations here: 
Dim width As ???
Dim value As ???
Dim numberArray() As ???
Dim nObtained As Integer
Dim nIntervals As ???
Dim min As ???
Dim max As ???
Dim kMaxCount As Integer
Dim k As Integer
Dim i As Integer
Dim file_name As ???
Dim fileNo As Integer
Dim count() As Integer
Rem  
fileNo = -10
Do
  PRINT "Name/path of the number file"; : INPUT file_name
  fileNo = fileOpen(file_name)
Loop Until fileNo > 0 OR file_name = ""
If fileNo > 0 Then
  fileClose(fileNo)
  PRINT "number of intervals"; : INPUT nIntervals
  Rem Initialize the interval counters 
  For k = 0 To nIntervals-1
    count(k) = 0
  Next k
  Rem Index of the most populated interval 
  kMaxCount = 0
  Let numberArray = Array()
  nObtained = 0
  Try
    nObtained = readNumbers(file_name, numberArray, 10000)
  Catch ex33723e30 As Exception
    Dim failure As String = ex33723e30.ToString()
    PRINT failure
  End Try
  If nObtained > 0 Then
    min = numberArray(0)
    max = numberArray(0)
    For i = 1 To nObtained-1
      If numberArray(i) < min Then
        min = numberArray(i)
      Elseif numberArray(i) > max Then
        max = numberArray(i)
      End If
    Next i
    Rem Interval width 
    width = (max - min) * 1.0 / nIntervals
    For i = 0 To nObtained - 1
      value = numberArray(i)
      k = 1
      Do While k < nIntervals AND value > min + k * width
        k = k + 1
      Loop
      count(k-1) = count(k-1) + 1
      If count(k-1) > count(kMaxCount) Then
        kMaxCount = k-1
      End If
    Next i
    Call drawBarChart(count, nIntervals)
    PRINT "Interval with max count: "; kMaxCount; " ("; count(kMaxCount); ")"
    For k = 0 To nIntervals-1
      PRINT count(k); " numbers in interval "; k; " ("; min + k * width; " ... "; min + (k+1) * width; ")"
    Next k
  Else
    PRINT "No numbers read."
  End If
End If
End
Rem  
Rem Draws a bar chart from the array "values" of size nValues. 
Rem Turtleizer must be activated and will scale the chart into a square of 
Rem 500 x 500 pixels 
Rem Note: The function is not robust against empty array or totally equal values. 
Rem TODO: Check (and specify if needed) the argument and result types! 
Sub drawBarChart(values As double(50), nValues)
  Rem TODO: Check and accomplish your variable declarations here: 
  Rem  
  Rem Used range of the Turtleizer screen 
  Const xSize = 500
  Const ySize = 500
  kMin = 0
  kMax = 0
  For k = 1 To nValues-1
    If values(k) > values(kMax) Then
      kMax = k
    Elseif values(k) < values(kMin) Then
      kMin = k
    End If
  Next k
  valMin = values(kMin)
  valMax = values(kMax)
  yScale = valMax * 1.0 / (ySize - 1)
  yAxis = ySize - 1
  If valMin < 0 Then
    If valMax > 0 Then
      yAxis = valMax * ySize * 1.0 / (valMax - valMin)
      yScale = (valMax - valMin) * 1.0 / (ySize - 1)
    Else
      yAxis = 1
      yScale = valMin * 1.0 / (ySize - 1)
    End If
  End If
  Rem draw coordinate axes 
  gotoXY(1, ySize - 1)
  forward(ySize -1) : Rem color = ffffff
  penUp()
  backward(yAxis) : Rem color = ffffff
  right(90)
  penDown()
  forward(xSize -1) : Rem color = ffffff
  penUp()
  backward(xSize-1) : Rem color = ffffff
  stripeWidth = xSize / nValues
  For k = 0 To nValues-1
    stripeHeight = values(k) * 1.0 / yScale
    Select Case k % 3
      Case 0
        setPenColor(255,0,0)
      Case 1
        setPenColor(0, 255,0)
      Case 2
        setPenColor(0, 0, 255)
    End Select
    fd(1) : Rem color = ffffff
    left(90)
    penDown()
    fd(stripeHeight) : Rem color = ffffff
    right(90)
    fd(stripeWidth - 1) : Rem color = ffffff
    right(90)
    forward(stripeHeight) : Rem color = ffffff
    left(90)
    penUp()
  Next k
End Sub

Rem = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

