#include <Aria.h>
#include "ArExport.h"
#include "ariaOSDef.h"
#include "ArMode.h"
#include "GalateiaControl.h"
#include "ArKeyHandler.h"
#include "ArSonyPTZ.h"
#include "ArVCC4.h"
#include "ArDPPTU.h"
#include "ArAMPTU.h"
#include "ArSick.h"
#include "ArAnalogGyro.h"
#include "ArRobotConfigPacketReader.h"
#include "ariaInternal.h"
#include <string>
#include <iostream>
#include <stdio.h>

using std::cout;
using std::endl;
using std::string;

#define U 10 //256 
#define D 5  //257
#define L 6  //258
#define R 9  //259
#define S 0  //261


	     
AREXPORT GalateiaControl::GalateiaControl(ArRobot *robot, const char *name, char key, char key2): ArMode(robot, name, key, key2)
{
   x = 0; y = 0; th = 0;
   x2 = 0; y2 = 0; th2 = 0;
   myRobot = robot;
   
   //inicializaçao da conexao com o servidor
   //de posição
   printf("\n********** Localização Inicializada ***********\n");
   Aria::init();

}

AREXPORT GalateiaControl::~GalateiaControl()
{
  
}

AREXPORT void GalateiaControl::activate(void){
	if (!baseActivate())
		return;
		
}

AREXPORT void GalateiaControl::deactivate(void){
	if (!baseDeactivate())
		return;
}

AREXPORT void GalateiaControl::userTask(void)
{
/*	x2 = myRobot->getX();
	y2 = myRobot->getY();
	th2 = myRobot->getTh();
	if((int)x != (int)x2 || (int)y != (int)y2 || (int)th != (int)th2){ 
		printf("\r(x, y, th) = (%10.0f, %10.0f, %10.0f)", x2, y2, th2);
		
		x = x2;
		y = y2;
		th = th2;
	}
//	printf("\rsonar 3 = %d   ", myRobot->getSonarRange(3));
		
//	printf("sonar 5 = %d", myRobot->getSonarRange(5));
	
//	if ( myRobot->getSonarRange(12) < 1733 || myRobot->getSonarRange(3) < 1733)
//		myRobot->stop();
	
	//teste de saida quando a conexÃo for finalizada
	if (positionSock.write(&x2, sizeof(x2)) != sizeof(x2)){
		printf("Conexao com o servidor finalizada \n");
        }
	  
	if (positionSock.write(&y2, sizeof(y2)) != sizeof(y2)){
	   	printf("Falha na conexao com o servidor \n");
	}
		   
	if (positionSock.write(&th2, sizeof(th2)) != sizeof(th2)){
	   	printf("Falha na conexao com o servidor \n");
	}*/
}


#define velocity 300

void GalateiaControl::controle(){
	string ip, com;
	char comando[10];

	bzero(comando,10);
	
	// abertura de conexão para o controle
	printf("\n***** Abertura da conexão com controle *****\n");
	if (serverSock.open(SERVERPORT, ArSocket::TCP)){
		printf("Porta servidor aberta %d \n", SERVERPORT);
	}
	else {
		printf("Falha ao abrir porta: %s\n",serverSock.getErrorStr().c_str());
		Aria::shutdown();
	}

	if (serverSock.accept(&clientSock)){
		printf("Cliente conectado\n\n");
		
		// chamada de sistema para a execução do servidor de vídeo
		ip = clientSock.getIPString();
	        com = "java RTPServer " + ip + "&";
	//	system(com.c_str());
	}else{
		printf("Erro ao aceitar conexao: %s\n",serverSock.getErrorStr().c_str());
		Aria::shutdown();
		return;
	}

	
	while(true){
		
       		if ( clientSock.read(comando, 10, 0) > 0 ) {

		     int test = 0;
			test = comando[0];
			test = test << 8 | comando[1];
			test = test << 16 | comando[2];
			test = test << 24 | comando[3];


			printf("unlocked \n");
		     printf("Comando: %i %d", comando[0], strlen(comando));
			
		     myComando = comando[0];
			bzero(comando,10);
		     if (myComando >= 0){
			 mutex.lock();

	       	      	 switch (myComando) {
				case  U: 
					myRobot->setVel2(velocity,velocity);
					break;
				case  D:
					myRobot->setVel2(-velocity,-velocity);
					break;
				case  L:
					myRobot->setVel2(-velocity,velocity);
					break;
				case  R:
					myRobot->setVel2(velocity,-velocity);
					break;
				case S:
					myRobot->stop();
					break;
       		 	 }
			 mutex.unlock();
		     }
		}
		else{
			printf("\n\nConexao com o controle fechada\n"); 
			clientSock.close();
		     	clientSock.shutdown();
			myRobot->stop();
		    
		      	// encerra a conexão de vídeo com o cliente
	//	     	system("./matar.sh RTPServer");
		     
		     	printf("\nEsperando novo cliente\n"); 
		     
		     	if (serverSock.accept(&clientSock)){
		       		printf("\nCliente conectado\n");
		     	
		     		//chamada de sistema para execução do servidor de video pra um novo cliente
				ip = clientSock.getIPString();
				com = "java RTPServer " + ip + "&";
	//			system(com.c_str());
		     	}
		     	else
				printf("Erro ao aceitar conexao: %s\n",serverSock.getErrorStr().c_str());
		   

		}
	}
}



int main(int argc, char** argv)
{
  // mandatory init
  Aria::init();

  // set up our parser
  ArArgumentParser parser(&argc, argv);
  // set up our simple connector
  ArSimpleConnector simpleConnector(&parser);
  // robot
  ArRobot robot;
  // sonar, must be added to the robot, for teleop and wander
  ArSonarDevice sonarDev;
  
  parser.loadDefaultArguments();

  // parse the command line... fail and print the help if the parsing fails
  // or if the help was requested
  if (!simpleConnector.parseArgs() || !parser.checkHelpAndWarnUnparsed())
  {    
    simpleConnector.logOptions();
    exit(1);
  }

  // a key handler so we can do our key handling
  ArKeyHandler keyHandler;
  // let the global aria stuff know about it
  Aria::setKeyHandler(&keyHandler);
  // toss it on the robot
  robot.attachKeyHandler(&keyHandler);
  // add the sonar to the robot
  robot.addRangeDevice(&sonarDev);

  // set up the robot for connecting
  if (!simpleConnector.connectRobot(&robot))
  {
  	printf("Could not connect to robot... exiting\n");
  	Aria::exit(1);
  }

  // start the robot running, true so that if we lose connection the run stops
  robot.runAsync(true);
 
  // ocupa a instancia do robô
  robot.lock();
 
  printf("\nVocê pode precionar ESC para sair\n");
  
  GalateiaControl galateia(&robot, "controle", '\0', '\0');
  
  // localização
  galateia.activate();

  // turn on the motors
  robot.comInt(ArCommands::ENABLE, 1);

  robot.unlock();
  
  // controle
  galateia.controle(); 
  
  robot.waitForRunExit();
  
  Aria::exit(0);


}
   
