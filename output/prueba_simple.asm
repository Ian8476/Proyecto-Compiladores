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

    # Declaración global: x
    li $t0, 5          # Cargar literal: 5
    # Fin inicialización de x
    # Declaración global: y
    li $t0, 3          # Cargar literal: 3
    # Fin inicialización de y
    # Declaración local: suma
    li $t0, 0          # Cargar literal: 0
    sw $t0, 0($fp)
    # Asignación: suma = ...
    # Operación: suma
    lw $t0, 0($sp)                 # Cargar variable: x
    lw $t0, 0($sp)                 # Cargar variable: y
    add $t0, $t0, $t1
    sw $t0, 0($fp)   # Guardar suma
    # Declaración local: producto
    li $t0, 0          # Cargar literal: 0
    sw $t0, -4($fp)
    # Asignación: producto = ...
    # Operación: multiplicación
    lw $t0, 0($sp)                 # Cargar variable: x
    lw $t0, 0($sp)                 # Cargar variable: y
    mult $t0, $t1
    mflo $t0
    sw $t0, -4($fp)   # Guardar producto

        # Epílogo: restaurar registros y retornar
        lw $ra, 4($sp)             # Restaurar dirección de retorno
        lw $fp, 0($sp)             # Restaurar frame pointer
        addi $sp, $sp, 8           # Liberar espacio en la pila
        jr $ra                     # Retornar al sistema operativo

    # Fin del programa
