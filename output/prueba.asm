.data
    # Sección de datos - variables globales
    newline: .asciiz "\n"
    space: .asciiz " "
    prompt_write: .asciiz "Valor: "
    globalNum: .word 0
    globalPi: .word 0
    flag: .word 0
    matriz: .word 0

.text
    .globl main
    main:
        # Prólogo: guardar registros y ajustar stack pointer
        addi $sp, $sp, -8          # Reservar espacio en la pila
        sw $ra, 4($sp)             # Guardar dirección de retorno
        sw $fp, 0($sp)             # Guardar frame pointer
        move $fp, $sp              # Establecer frame pointer (base del frame)

    # Declaración global: globalNum
    li $t0, 10          # literal int
    sw $t0, globalNum
    # Fin inicialización de globalNum
    # Declaración global: globalPi
    li $t0, 0
    sw $t0, globalPi
    # Fin inicialización de globalPi
    # Declaración global: flag
    li $t0, 1          # literal bool
    sw $t0, flag
    # Fin inicialización de flag
    # Declaración global: matriz
    addi $sp, $sp, -4
    # Declaración local: a
    li $t0, 5          # literal int
    sw $t0, -4($fp)
    addi $sp, $sp, -4
    # Declaración local: b
    li $t0, 0
    sw $t0, -8($fp)
    addi $sp, $sp, -4
    # Declaración local: cond
    li $t0, 0          # literal bool
    sw $t0, -12($fp)
    # Asignación: a = ...
    lw $t0, -4($fp)   # local a
    li $t1, 3          # literal int
    add $t0, $t0, $t1
    sw $t0, -4($fp)   # Guardar a
    # Asignación: b = ...
    lw $t0, -8($fp)   # local b
    li $t1, 0
    bne $t1, $zero, L_div_ok_0
    li $t0, 0
    j L_div_end_1
L_div_ok_0:
    div $t0, $t1
    mflo $t0
L_div_end_1:
    sw $t0, -8($fp)   # Guardar b
    addi $sp, $sp, -4
    # Declaración local: potencia
    li $t0, 2          # literal int
    li $t1, 3          # literal int
    li $t2, 1
L_pow_loop_2:
    blez $t1, L_pow_end_3
    mult $t2, $t0
    mflo $t2
    addi $t1, $t1, -1
    j L_pow_loop_2
L_pow_end_3:
    move $t0, $t2
    sw $t0, -16($fp)
    lw $t0, -4($fp)   # local a
    addi $t0, $t0, 1
    sw $t0, -4($fp)   # guardar local a
    move $a0, $t0
    li $v0, 1
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    lw $t0, -8($fp)   # local b
    addi $t0, $t0, -1
    sw $t0, -8($fp)   # guardar local b
    move $a0, $t0
    li $v0, 1
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    # Asignación: a = ...
    li $t0, 0          # literal int
    sw $t0, -4($fp)   # Guardar a
    L4:
    lw $t0, -4($fp)   # local a
    li $t1, 5          # literal int
    slt $t0, $t0, $t1
    beq $t0, $zero, L5
    addi $sp, $sp, -4
    # Declaración local: temp
    lw $t0, -4($fp)   # local a
    li $t1, 2          # literal int
    mult $t0, $t1
    mflo $t0
    sw $t0, -20($fp)
    # Liberar espacio de locales
    addi $sp, $sp, 4
    lw $t0, -4($fp)   # local a
    addi $t0, $t0, 1
    sw $t0, -4($fp)   # guardar local a
    j L4
    L5:
    addi $sp, $sp, -4
    # Declaración local: j
    li $t0, 0          # literal int
    sw $t0, -20($fp)
    L6:
    # Cuerpo del loop
    # Asignación: j = ...
    lw $t0, -20($fp)   # local j
    li $t1, 1          # literal int
    add $t0, $t0, $t1
    sw $t0, -20($fp)   # Guardar j
    # Condición de salida
    lw $t0, -20($fp)   # local j
    li $t1, 3          # literal int
    slt $t0, $t0, $t1
    xori $t0, $t0, 1
    bne $t0, $zero, L7
    j L6
    L7:
    # Asignación: cond = ...
    lw $t0, -4($fp)   # local a
    li $t1, 3          # literal int
    slt $t0, $t1, $t0
    lw $t1, -8($fp)   # local b
    li $t2, 0
    slt $t1, $t1, $t2
    sltu $t0, $zero, $t0
    sltu $t1, $zero, $t1
    and $t0, $t0, $t1
    sw $t0, -12($fp)   # Guardar cond
    addi $sp, $sp, -4
    # Declaración local: neg
    lw $t0, -12($fp)   # local cond
    sltu $t0, $zero, $t0
    xori $t0, $t0, 1
    sw $t0, -24($fp)
    # Estructura DECIDE
    # Caso
    lw $t0, -4($fp)   # local a
    li $t1, 5          # literal int
    xor $t0, $t0, $t1
    sltiu $t0, $t0, 1
    beq $t0, $zero, L9
    addi $sp, $sp, -4
    # Declaración local: caso1
    li $t0, 1          # literal int
    sw $t0, -28($fp)
    # Liberar espacio de locales
    addi $sp, $sp, 4
    j L8
    L9:
    addi $sp, $sp, -4
    # Declaración local: caso2
    li $t0, 2          # literal int
    sw $t0, -28($fp)
    # Liberar espacio de locales
    addi $sp, $sp, 4
    L8:
    li $t0, 0          # literal int
    move $v0, $t0             # Mover resultado a $v0
    j L_program_end        # return en main -> salir del programa
    # Liberar espacio de locales
    addi $sp, $sp, 24

L_program_end:
        # Epílogo: restaurar registros y retornar
        move $sp, $fp              # Restaurar stack pointer al frame
        lw $ra, 4($sp)             # Restaurar dirección de retorno
        lw $fp, 0($sp)             # Restaurar frame pointer
        addi $sp, $sp, 8           # Liberar espacio en la pila
        li $v0, 10                 # Salir (QtSPIM)
        syscall

    # Fin del programa
