.data
    # Sección de datos - variables globales irían aquí
    newline: .asciiz "\n"

.text
    .globl main
    main:
        # Prólogo: guardar registros y ajustar stack pointer
        addi $sp, $sp, -4          # Reservar espacio en la pila
        sw $ra, 0($sp)             # Guardar dirección de retorno

    # Declaración global: numero
    li $t0, 42          # Cargar literal: 42
    # Fin inicialización de numero
    li $t0, 0          # Cargar literal: 0
    # Asignación: resultado = ...
    # Operación: suma
    lw $t0, 0($sp)                 # Cargar variable: numero
    li $t0, 10          # Cargar literal: 10
    add $t0, $t0, $t1
    # Resultado en $t0, guardando en variable: resultado
    sw $t0, -4($sp)   # Guardar valor de resultado

        # Epílogo: restaurar registros y retornar
        lw $ra, 0($sp)             # Restaurar dirección de retorno
        addi $sp, $sp, 4           # Liberar espacio en la pila
        jr $ra                     # Retornar al sistema operativo

    # Fin del programa
