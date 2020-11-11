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
0:  ADDI R1, R0, 4004
4:  ADDI R2, R0, 4004
8:  LW R3, 4000(R0)
12:  SLL R3, R3, 2
16:  ADD R3, R3, R1
20:  ADDI R3, R3, -4
24:  ADDI R30, R0, 5000
      --
28:  JAL QuickSort
32:  J End
      --
LABEL QuickSort
36:  SW R31, 0(R30)
40:  ADDI R30, R30, 4
44:  SUB R10, R3, R2
48:  BLEZ R10, EndOfQS
      --
52:  LW R4, 0(R3)
56:  ADDI R5, R2, -4
60:  ADDI R6, R2, 0
      --
LABEL StartOfLoop
64:  SUB R11, R3, R6
68:  BLEZ R11, EndOfLoop
72:  LW R8, 0(R6)
76:  SUB R12, R4, R8
80:  BLTZ R12, EndOfIf
      --
84:  ADDI R5, R5, 4
88:  LW R7, 0(R5)
92:  SW R8, 0(R5)
96:  SW R7, 0(R6)
LABEL EndOfIf
100:  ADDI R6, R6, 4
104:  J StartOfLoop
      --
LABEL EndOfLoop
108:  ADDI R5, R5, 4
112:  LW R7, 0(R5)
116:  SW R4, 0(R5)
120:  SW R7, 0(R3)
      --
124:  SW R3, 0(R30)
128:  SW R5, 4(R30)
132:  ADDI R30, R30, 8
      --
136:  ADDI R3, R5, -4
140:  JAL QuickSort
      --
144:  ADDI R30, R30, -8
148:  LW R3, 0(R30)
152:  LW R2, 4(R30)
156:  ADDI R2, R2, 4
160:  JAL QuickSort
      --
LABEL EndOfQS
164:  ADDI R30, R30, -4
168:  LW R31, 0(R30)
172:  JR R31
      --
LABEL End
176:  HALT
