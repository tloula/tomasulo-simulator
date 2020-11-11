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
0:  LW R1, 4000(R0)
4:  SLL R1, R1, 2
8:  ADDI R1, R1, -4
12:  ADDI R2, R0, 0
      --
LABEL OuterLoop
16:  BEQ R1, R2, PostOuter
20:  SUB R10, R1, R2
24:  ADDI R3, R0, 0
      --
LABEL InnerLoop
28:  BEQ R10, R3, PostInner
      --
32:  LW R7, 4004(R3)
36:  LW R8, 4008(R3)
40:  SUB R9, R7, R8
44:  BLTZ R9, EndSwap
      --
48:  SW R7, 4008(R3)
52:  SW R8, 4004(R3)
      --
LABEL EndSwap
56:  ADDI R3, R3, 4
60:  J InnerLoop
      --
LABEL PostInner
64:  ADDI R2, R2, 4
68:  J OuterLoop
      --
LABEL PostOuter
72:  HALT
