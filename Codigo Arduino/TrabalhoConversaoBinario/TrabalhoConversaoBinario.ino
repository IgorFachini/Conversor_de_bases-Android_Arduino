

String numeros;
int posicao = 0;
void setup() {

  Serial.begin(9600);
  Serial.setTimeout(9999999);
 
}

void loop() {
  //apagaLeds();
    numeros = (String)Serial.readStringUntil(';');
    Serial.println(numeros);
    apagaLeds();
   for (int i = 0; i < numeros.length(); i++) {
    char bi = (char)numeros[i];
    switch (i) {
      case 0:
       if (bi == '1') {acendLed(2);}
        break;
      case 1:
      if (bi == '1') {acendLed(3);}
        break;
      case 2:
      if (bi == '1') {acendLed(4);}
        break;
      case 3:
      if (bi == '1') {acendLed(5);}
        break;
      case 4:
      if (bi == '1') {acendLed(6);}
        break;
      case 5:
      if (bi == '1') {acendLed(7);}
        break;
      case 6:
      if (bi == '1') {acendLed(8);}
        break;
      case 7:
      if (bi == '1') {acendLed(9);}
        break;

    }   
  }
  numeros = "";
}
void acendLed(int led){
   analogWrite(led, 255);
}

void apagaLed(int led){
   analogWrite(led, 0);
}

void apagaLeds() {
  analogWrite(2, 0);
  analogWrite(3, 0);
  analogWrite(4, 0);
  analogWrite(5, 0);
  analogWrite(6, 0);
  analogWrite(7, 0);
  analogWrite(8, 0);
  analogWrite(9, 0);
}

