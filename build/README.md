## Programmet kan startas med ett antal parametrar enligt följande:

java -jar SofthouseTest.jar (input-file) (output-file) (allowDuplicateInfo[true/false])

### input-file
En fil vars text-innehåll representerar data enligt det gamla systemets struktur. (default är hårdkodat test-data)

### output-file
Data enligt det nya formatet skrivs ut här. (default är console output)

### allowDuplicateInfo
Huruvida vi ska tillåta att En individ i det gamla formatet uppföljs av två rader med information angående t.ex adress. 
Vid true: den sista rader är relevant
Vid false: ett felmeddelande visas 
