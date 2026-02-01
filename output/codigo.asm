.data
    # Sección de datos - variables globales irían aquí
    newline: .asciiz "\n"

.text
    .globl main
    main:
        # Prólogo: guardar registros y ajustar stack pointer
        addi $sp, $sp, -4          # Reservar espacio en la pila
        sw $ra, 0($sp)             # Guardar dirección de retorno

    # Declaración global: resultado
    li $t0, 2          # Cargar literal: 2
    # Fin inicialización de resultado
    # Declaración global: limite
    li $t0, 5          # Cargar literal: 5
    # Fin inicialización de limite
    li $t0, 0          # Cargar literal: 0
    # Asignación: i = ...
    li $t0, 0          # Cargar literal: 0
    # Resultado en $t0, guardando en variable: i
    sw $t0, -4($sp)   # Guardar valor de i
    # Operación: menor que
    lw $t0, 0($sp)                 # Cargar variable: i
    li $t0, 10          # Cargar literal: 10
    blt $t0, $t1, L0
    # Operación unaria: ++
    # Asignación: resultado = ...
    # Operación: suma
    lw $t0, 0($sp)                 # Cargar variable: resultado
    li $t0, 1          # Cargar literal: 1
    add $t0, $t0, $t1
    # Resultado en $t0, guardando en variable: resultado
    sw $t0, -8($sp)   # Guardar valor de resultado
    # Operación: igualdad
    lw $t0, 0($sp)                 # Cargar variable: resultado
    li $t0, 10          # Cargar literal: 10
    beq $t0, $t1, L1
    li $t0, 1          # Cargar literal: 1
    # Operación: menor que
    lw $t0, 0($sp)                 # Cargar variable: resultado
    li $t0, 5          # Cargar literal: 5
    blt $t0, $t1, L2
    li $t0, 1          # Cargar literal: 1
    li $t0, 0          # Cargar literal: 0

        # Epílogo: restaurar registros y retornar
        lw $ra, 0($sp)             # Restaurar dirección de retorno
        addi $sp, $sp, 4           # Liberar espacio en la pila
        jr $ra                     # Retornar al sistema operativo

    # Fin del programa
