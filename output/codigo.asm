.data
    # Sección de datos - variables globales irían aquí
    newline: .asciiz "\n"

.text
    .globl main
    main:
        # Prólogo: guardar registros y ajustar stack pointer
        addi $sp, $sp, -4          # Reservar espacio en la pila
        sw $ra, 0($sp)             # Guardar dirección de retorno

    # Declaración global: x
    # Declaración global: s
    # Operación: suma
    lw $t0, 0($sp)                 # Cargar variable: x
    lw $t0, 0($sp)                 # Cargar variable: s
    add $t0, $t0, $t1

        # Epílogo: restaurar registros y retornar
        lw $ra, 0($sp)             # Restaurar dirección de retorno
        addi $sp, $sp, 4           # Liberar espacio en la pila
        jr $ra                     # Retornar al sistema operativo

    # Fin del programa
