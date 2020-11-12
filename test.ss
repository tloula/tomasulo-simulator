0:  ADDI R1, R0, 4000
4:  ADDI R2, R0, 5
8:  SW R2, 0(R1)
12:  SW R2, 4(R1)
16:  SW R1, 8(R1)
20:  HALT
      --
      --
      --
      --
      --
      --ADDI R10, R0, 9
      --LABEL LOOP
      --ADDI R1, R1, 1
      --ADDI R2, R2, 2
      --ADDI R3, R3, 3
      --BEQ R3, R10, END
      --ADDI R4, R4, 4
      --ADDI R5, R5, 5
      --JAL LOOP
      --ADDI R6, R0, 6
      --ADDI R7, R0, 7
      --LABEL END
      --JR R31
      --ADDI R8, R0, 8
      --ADDI R9, R0, 9
      --HALT
