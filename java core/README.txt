Note per la tesi

Parti 'mal progettati'
- CalcolaIndiciLocalita.java BOH



Comando Prompt DOS
- Andare nella dove riporre le classi
- compilare -- javac -d ..\.class ..\*.java
- eseguire  -- java Main > ..\JavaOutput.txt


- Parlare della conversione e gestione di dati in Java
    - Array dinamici
    - Uniformità dei dati di imput
    - Utilizzo dell'algoritmo di Sobel per implementare e migliorare l'algorito ??? in Python
    - Implementazione di un UI
    - Implementazione di un sistema di API
    - Implementazione di reportistica.
    - Calcolo dei risultati in realtime (magari facendo un video nelle slide)


// Pseudo codice delle funzioni chiamanti

- Main -> MediaVarianzaAutomaticaPivotFinale(Storico,Percorso)

- MediaVarianzaAutomaticaPivotFinale(Storico , Percorso)
    for indice as indice[]
        for precisioni  as precisioni[]
            CalcoloMacc( pathFile , metrilato , indice);
            for deltatime ad deltatime[]
                esecuzioneTest(risTemp , risPerc , pathFilePercorsoTest , pathFile , metrilato , deltaTimeMassimo , indice , stringaDeltaTime);

- MediaVarianzaAutomaticaPivotFinale( File , metriLato , numeroIterazioni )
    SRmatriciDizionari(File,metriLato,numeroIterazioni)


- esecuzioneTest( risTemp, risPerc, pathFilePercorsoTest, pathFile, metriLato, deltaTimeMassimo, indice, stringaDeltaTime)
    - Calcolo i percorsi tenendo contro del deltaTimeMassimo per punto
    - Per ogni percorso, i risultati ARDA partendo dal 0, 25, 50 e 75 % del percorso e valuto il risultato.
        ARDA.predict_next_for(actual_point,prev_point,Macc,Mimp,grid_info,afterTime,maxBrakeValue);
    - Con i risultati (per ogni percentuale e i complessivi)

- ARDA_algorith
    - Non capisco perché usa floor per le posizioin in get_value e get_brakevalue!!!
    - single_object_prediction in java restituisce anche il punto precedente... Perchè?
    - predict_next_for è in 2 versioni... perché?
    - come mai la seconda non viene usata? E sopratutto perchè non viene implementata in python?
