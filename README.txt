================================
JBudget - Istruzioni di avvio
================================

Per avviare l'applicazione:

1.  Apri un terminale o un prompt dei comandi nel root della cartella.
2.  Esegui il comando:

    - Su Windows:
      .\gradlew run

    - Su macOS/Linux:
      ./gradlew run

--- Database ---

L'applicazione utilizza un database H2 su file per memorizzare tutti i dati. Ho fornito un database con dei dati di partenza già disponibili per il testing.
Il file del database verrà creato automaticamente al primo avvio e si troverà nel seguente percorso relativo alla cartella del progetto:

`./data/database.mv.db`

È importante non cancellare questa cartella per non perdere i dati salvati.
