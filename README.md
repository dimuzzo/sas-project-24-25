# sas-project-24-25

Compiling su laptop:
- Installare SQLite e inserire il PATH nelle variabili di sistema
- Installare Apache Maven ed inserire il PATH sia nelle variabili di sistema sia nelle variabili di VSCode
- I test funzionano correttamente tutti e 12
- Maven -> Lifecycle -> clean -> compile -> test

- EXTRA: La Risposta per l'Esame
Se il professore nota la differenza tra Role.create() e Staff.save(), la tua risposta può essere:

"Assolutamente. È una differenza voluta che ho mantenuto come punto di riflessione pratica sui principi di progettazione e refactoring che abbiamo studiato.

Nel progetto, ho esplorato due pattern di persistenza validi:

Per classi come Staff e HolidaysRequest, ho usato il pattern Active Record, dove l'oggetto stesso sa come salvarsi con un metodo save(). È uno stile molto leggibile e allineato ai principi di semplicità dell'Extreme Programming.

Per Role, ho invece usato un metodo statico create() che lancia una SQLException. Questo stile favorisce una maggiore separazione delle responsabilità e una gestione degli errori più esplicita, obbligando il chiamante a gestire una checked exception.

Una volta implementati entrambi e verificato con i test che funzionassero, ho affrontato una classica decisione di refactoring: dovrei standardizzare?

Qui ho applicato un altro principio chiave: il pragmatismo. Poiché il codice era completamente funzionante e coperto da test, un refactoring dell'ultimo minuto sulla classe Role avrebbe introdotto un rischio inutile senza aggiungere nuovo valore funzionale. Ho quindi deciso, in accordo con la pratica di non modificare codice funzionante senza una forte necessità, di mantenere questa dualità come testimonianza del mio processo di apprendimento. Dimostra la mia capacità di implementare diversi pattern e, soprattutto, di valutare criticamente quando un refactoring è davvero necessario e quando è più saggio privilegiare la stabilità."