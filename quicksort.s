-- Bubble Sort Assembly Program
-- 4000 number of numbers to sort
-- 4004 beginning of array of numbers
-- 5000 beginning of stack
--
-- Register variables
-- R1 = Array
-- R2 = low pointer
-- R3 = high pointer
-- R4 = pivot
-- R5 = i, it is actually a pointer to arr[i]
-- R6 = j, loop variable, also a pointer to arr[j]
-- R7 = arr[i]
-- R8 = arr[j]
-- R9 = arr[high]
-- R10 = high - low
-- R11 = high - j - arr
-- R12 = pivot - arr[j]
-- R30 = stack pointer
--
Begin Assembly
ADDI R1, R0, 4004
ADDI R2, R0, 4004
LW R3, 4000(R0)
SLL R3, R3, 2
ADD R3, R3, R1
ADDI R3, R3, -4
ADDI R30, R0, 5000
--
JAL QuickSort
J End
--
LABEL QuickSort
SW R31, 0(R30)
ADDI R30, R30, 4
SUB R10, R3, R2
BLEZ R10, EndOfQS
--
LW R4, 0(R3)
ADDI R5, R2, -4
ADDI R6, R2, 0
--
LABEL StartOfLoop
SUB R11, R3, R6
BLEZ R11, EndOfLoop
LW R8, 0(R6)
SUB R12, R4, R8
BLTZ R12, EndOfIf
--
ADDI R5, R5, 4
LW R7, 0(R5)
SW R8, 0(R5)
SW R7, 0(R6)
LABEL EndOfIf
ADDI R6, R6, 4
J StartOfLoop
--
LABEL EndOfLoop
ADDI R5, R5, 4
LW R7, 0(R5)
SW R4, 0(R5)
SW R7, 0(R3)
--
SW R3, 0(R30)
SW R5, 4(R30)
ADDI R30, R30, 8
--
ADDI R3, R5, -4
JAL QuickSort
--
ADDI R30, R30, -8
LW R3, 0(R30)
LW R2, 4(R30)
ADDI R2, R2, 4
JAL QuickSort
--
LABEL EndOfQS
ADDI R30, R30, -4
LW R31, 0(R30)
JR R31
--
LABEL End
HALT
End Assembly
Begin Data 4000 68
16
844
757
420
258
511
404
783
303
476
583
908
504
281
755
618
250
End Data