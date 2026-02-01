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

    # Declaración global: numero
    li $t0, 42          # Cargar literal: 42
    # Fin inicialización de numero
    # Declaración local: resultado
    li $t0, 0          # Cargar literal: 0
    sw $t0, 0($fp)
    # Asignación: resultado = ...
    # Operación: suma
    lw $t0, 0($sp)                 # Cargar variable: numero
    li $t0, 10          # Cargar literal: 10
    add $t0, $t0, $t1
    sw $t0, 0($fp)   # Guardar resultado

        # Epílogo: restaurar registros y retornar
        lw $ra, 4($sp)             # Restaurar dirección de retorno
        lw $fp, 0($sp)             # Restaurar frame pointer
        addi $sp, $sp, 8           # Liberar espacio en la pila
        jr $ra                     # Retornar al sistema operativo

    # Fin del programa
