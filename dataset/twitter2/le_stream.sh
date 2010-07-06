#!/bin/bash

#curl -usnbuback_test:mucuxi "http://stream.twitter.com/1/statuses/filter.json" -d "track=:(" | grep -i Brazil > negativo.json
#curl -usnbuback_test:mucuxi "http://stream.twitter.com/1/statuses/filter.json" -d "track=:)" | grep -i Brazil > positivo.json

#### negativo
cat negativo.json | grep "in_reply_to_status_id\":null" |  grep -v retweeted_status | sed -e 's/[{}]/''/g' -e 's/,[^"]/ /g' | awk -v k="text" '{n=split($0,a,","); for (i=1; i<=n; i++)  print a[i]}' | grep \"text\" | cut -b 9- | tr -d \" |  ascii2uni -a U | sort | uniq > negativo-texto-emoticon.txt 

# Retira os emoticons e embaralha o texto
cat negativo-texto-emoticon.txt | tr -d ":\\-(" | tr -d ":(" |  tr -d ":\\-)" | tr -d ":)" | ./random > negativo-sem-emoticon.txt

# Separa em 500 para treino e o restante para o teste
cat negativo-sem-emoticon.txt | head -n 500 > negativo-treino.txt
cat negativo-sem-emoticon.txt | tail -n +500 > negativo-teste.txt

#### positivo
cat positivo.json | grep "in_reply_to_status_id\":null" |  grep -v retweeted_status | sed -e 's/[{}]/''/g' -e 's/,[^"]/ /g' | awk -v k="text" '{n=split($0,a,","); for (i=1; i<=n; i++)  print a[i]}' | grep \"text\" | cut -b 9- | tr -d \" |  ascii2uni -a U | sort | uniq > positivo-texto-emoticon.txt 

# Retira os emoticons e embaralha o texto
cat positivo-texto-emoticon.txt | tr -d ":\\-(" | tr -d ":(" |  tr -d ":\\-)" | tr -d ":)" | ./random > positivo-sem-emoticon.txt

# Separa em 500 para treino e o restante para o teste
cat positivo-sem-emoticon.txt | head -n 500 > positivo-treino.txt
cat positivo-sem-emoticon.txt | tail -n +500 > positivo-teste.txt



