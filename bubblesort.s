-- Bubble Sort Assembly Program
-- 4000 number of numbers to sort
-- 4004 beginning of array of numbers
--
-- Register variables
-- R1 = n/n - 1, i.e. size of array
-- R2 = i, outer loop variable
-- R3 = j, inner loop variable
-- R4 = array pointer
-- R7 = arr[j]
-- R8 = arr[j+1]
-- R9 = arr[j] - arr[j + 1]
-- R10 = n - i - 1
-- R11 = tmp, for swap function
-- R31 = Return address
--
Begin Assembly
LW R1, 4000(R0)
SLL R1, R1, 2
ADDI R1, R1, -4
ADDI R2, R0, 0
--
LABEL OuterLoop
BEQ R1, R2, PostOuter
SUB R10, R1, R2
ADDI R3, R0, 0
--
LABEL InnerLoop
BEQ R10, R3, PostInner
--
LW R7, 4004(R3)
LW R8, 4008(R3)
SUB R9, R7, R8
BLTZ R9, EndSwap
--
SW R7, 4008(R3)
SW R8, 4004(R3)
--
LABEL EndSwap
ADDI R3, R3, 4
J InnerLoop
--
LABEL PostInner
ADDI R2, R2, 4
J OuterLoop
--
LABEL PostOuter
HALT
End Assembly
Begin Data 4000 68
16
864
394
776
911
430
41
265
988
523
497
414
940
802
849
310
991
End Data