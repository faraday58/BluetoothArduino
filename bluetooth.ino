//bluetooth h05
int LED=12; //Configurando la salida en el pin 13
int state=0; //Variable para la lectura del puerto serial
int bandera=0;
int VelBaud=9600;

void setup() {
  pinMode(LED,OUTPUT); //Configurando el pin como salidad
  digitalWrite(LED, LOW); //Se mantenga el LED apagado
  Serial.begin(VelBaud);  

}

void loop() {

  if(Serial.available() > 0)
  {
    state = Serial.read();
  }

  if(state == 'A' && bandera == 0)
  {
    digitalWrite(LED, HIGH);
    bandera=1;
    state=0;  
  }

  if(state == 'A' && bandera  == 1)
  {
    digitalWrite(LED,LOW);
    bandera = 0;
    state = 0;
  }
  
  
}
