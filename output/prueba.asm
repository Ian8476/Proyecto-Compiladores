.data
    # Sección de datos - variables globales
    newline: .asciiz "\n"
    space: .asciiz " "
    prompt_write: .asciiz "Valor: "

.text
    .globl main
    main:
        # Prólogo: guardar registros y ajustar stack pointer
        addi $sp, $sp, -8          # Reservar espacio en la pila
        sw $ra, 4($sp)             # Guardar dirección de retorno
        sw $fp, 0($sp)             # Guardar frame pointer
        addi $fp, $sp, 8           # Establecer nuevo frame pointer

    # Declaración global: resultado
    li $t0, 2          # Cargar literal: 2
    # Fin inicialización de resultado
    # Declaración global: limite
    li $t0, 5          # Cargar literal: 5
    # Fin inicialización de limite
    # Declaración local: i
    li $t0, 0          # Cargar literal: 0
    sw $t0, 0($fp)
    # Asignación: i = ...
    li $t0, 0          # Cargar literal: 0
    sw $t0, 0($fp)   # Guardar i
    L0:
    # Operación: menor que
    lw $t0, 0($sp)                 # Cargar variable: i
    li $t0, 10          # Cargar literal: 10
    blt $t0, $t1, L2
    beq $t0, $zero, L1
    # Asignación: resultado = ...
    # Operación: suma
    lw $t0, 0($sp)                 # Cargar variable: resultado
    li $t0, 1          # Cargar literal: 1
    add $t0, $t0, $t1
    sw $t0, -4($gp)            # Guardar variable global: resultado
    # Operación unaria: ++
    j L0
    L1:
    # Estructura DECIDE
    # Caso
    # Operación: igualdad
    lw $t0, 0($sp)                 # Cargar variable: resultado
    li $t0, 10          # Cargar literal: 10
    beq $t0, $t1, L5
    beq $t0, $zero, L4
    # Declaración local: exito
    li $t0, 1          # Cargar literal: 1
    sw $t0, -4($fp)
    j L3
    L4:
    # Caso
    # Operación: menor que
    lw $t0, 0($sp)                 # Cargar variable: resultado
    li $t0, 5          # Cargar literal: 5
    blt $t0, $t1, L7
    beq $t0, $zero, L6
    # Declaración local: incompleto
    li $t0, 1          # Cargar literal: 1
    sw $t0, -8($fp)
    j L3
    L6:
    # Declaración local: otros
    li $t0, 0          # Cargar literal: 0
    sw $t0, -12($fp)
    L3:

        # Epílogo: restaurar registros y retornar
        lw $ra, 4($sp)             # Restaurar dirección de retorno
        lw $fp, 0($sp)             # Restaurar frame pointer
        addi $sp, $sp, 8           # Liberar espacio en la pila
        jr $ra                     # Retornar al sistema operativo

    # Fin del programa
