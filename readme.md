
# TxQApp

## Errores con toques muy rapidos o wifi lento
Se proponen las siguientes soluciones para optimizar el envio de mensajes entre las dos apps.
#### Optimizacion de sockets
https://chatgpt.com/share/682f3020-4754-8002-894a-ff0ffa82e21c

#### Posible Bug
Cuando se recibe la informacion de que la otra tablet ha recibido un toque se llama a la funcion randomBlink() de la clase GridViewCanvas, es posible que al generar los numeros de la fila y columna de la celda a iluminar la funcion usada (Random.nextInt) devuelva integers fuera del rango de la cuadricula, resultando en que se ilumine una celda fuera del rango visual.

## Propuesta de desarrollo para patrones de parpadeos
Una tablet empieza en el turno y la otra esta a la escucha

Cada tablet tendra un Temporizador que guarde el momento (fecha) de los pulsos que le lleguen en un array. Cuando pase X tiempo sin recibir ningun mensaje, esta entendera que el turno ha cambiado y forma una respuesta teniendo en cuenta los intervalos entre los pulsos recibidos.

Para crear la respuesta, se elegira aleatoriamente entre tres opciones, igualar el ritmo, subirlo o bajarlo.

En caso de bajarlo, se aplicara un delta que disminuya la el ritmo del patron original, creando un patron nuevo con posiblemente menos pulsos y separadas por mas tiempo.

En caso de igualarlo el delta aplicado al ritmo y numero de pulsaciones es muy pequeno, pero puede ser positivo o negativo.

En caso de aumentarlo, el delta para pulsaciones es mayor y con menor intervalo.