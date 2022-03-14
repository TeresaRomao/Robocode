package man;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import robocode.*;
import robocode.util.Utils;

public class MyFirstRobotTW extends AdvancedRobot {

	private EnemyAdvance enemy = new EnemyAdvance();
	
	int strategy = 3;

	int moveDirection = 1 ;
	int radarDirection = 1;
	boolean peek; // qão virar se há um robot
	double moveAmount; // quanto mover
	private double length = 64;
	
	 private boolean moved = false; // se precisarmos nos mover ou virar
	 private boolean inCorner = false; // se estiver nas paredes
	 private String targ; // que robô mirar
	 private byte spins = 0; // contador de giros do robô
	 private byte dir = 1; // Direção para mover
	 private short prevE; //energia anterior do robô que estamos a atirar

    public void run() {
    	setBodyColor(new Color(204, 153, 255));
    	setGunColor(new Color(255, 153, 255));
    	setRadarColor(new Color(255, 0, 127));
		setScanColor(Color.white);
		setBulletColor(Color.pink);
        while (true) { //sempre verdadeiro, não pode terminar o método run até o final da partida
        	//onRobotDeath();
        	if(strategy==1) {
        	doMove();
        	}
        	if(strategy == 2) {        		
        		doGun();
        		doRadar();
        	}
        	if(strategy == 3) {
        		turnRadarLeftRadians(1); // continuamente a girar o radar para a esquerda
        		setTurnRadarRight(360); // Roda o radar
        	}
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
    	double distance = e.getDistance(); // distância do robot que fizemos scann
        if(strategy == 1){
					if(distance > 800) //estas condições ajustam a força do tiro de acordo com a distância do robot scanned.
            fire(1);
        else if(distance > 600 && distance <= 800)
            fire(2);
        else if(distance > 400 && distance <= 600)
            fire(3);
        else if(distance > 200 && distance <= 400)
            fire(4);
        else if(distance < 200)
            fire(5);
					}

        Move(e);
        enemy.update(e);
        
        if(strategy == 3) {
        	// se não tivermos um alvo, vamos escolher o primeiro robô scanned
        	  if (targ == null || spins > 6) {
        	   targ = e.getName();
        	  }

        	  // Movimento circular com fuga de paredes	  
        	  circularMove(e);

        	  // Estratégia de tiro circular
        	  if (getDistanceRemaining() == 0 && getTurnRemaining() == 0) { // não se movendo ou girando

        	   // Se tivermos nas paredes, verificamos se nos movemos antes
        	   if (inCorner) {
        	    // Se nos movemos antes, então movemos de forma circular pra esquerda num angulo de 90 graus 
        	    if (moved) {
        	     setTurnLeft(90);
        	     moved = false; 
        	    } else { 
        	     setAhead(160 * dir); 
        	     moved = true; 
        	    }
        	   } else {
        	    // se não estamos indo N / S ir para o norte ou para o sul
        	    if ((getHeading() % 90) != 0) {
        	     setTurnLeft((getY() > (getBattleFieldHeight() / 2)) ? getHeading() :
        	      getHeading() - 180);
        	    }
        	    // se não estivermos no topo ou no fundo, vá para o que estiver mais perto
        	    else if (getY() > 30 && getY() < getBattleFieldHeight() - 30) {
        	     setAhead(getHeading() > 90 ? getY() - 20 : getBattleFieldHeight() - getY() -
        	      20);
        	    }
        	    // se não estivermos voltados para leste / oeste, viramos para ele
        	    else if (getHeading() != 90 && getHeading() != 270) {
        	     if (getX() < 350) {
        	      setTurnLeft(getY() > 300 ? 90 : -90);
        	     } else {
        	      setTurnLeft(getY() > 300 ? -90 : 90);
        	     }
        	    }
        	    // se não estivermos à esquerda ou à direita, vá para o que estiver mais perto
        	    else if (getX() > 30 && getX() < getBattleFieldWidth() - 30) {
        	     setAhead(getHeading() < 180 ? getX() - 20 : getBattleFieldWidth() - getX() -
        	      20);
        	    }
        	    // estamos no canto; vire e comece a se mover
        	    else if (getHeading() == 270) {
        	     setTurnLeft(getY() > 200 ? 90 : 180);
        	     inCorner = true;
        	    }
        	    // estamos no canto; vire e comece a se mover
        	    else if (getHeading() == 90) {
        	     setTurnLeft(getY() > 200 ? 180 : 90);
        	     inCorner = true;
        	    }
        	   }
        	  }
        	  if (e.getName().equals(targ)) { // se o robot scanned é o nosso target
        	   spins = 0; // reset o radar

        	   // se o inimigo disparar, com 15% de chance muda a direção 
        	   if ((prevE < (prevE = (short) e.getEnergy())) && Math.random() > .85) {
        	    dir *= -1;
        	   }

        	   // Mova a arma na direção deles
        	   setTurnGunRightRadians(Utils.normalRelativeAngle((getHeadingRadians() + e
        	    .getBearingRadians()) - getGunHeadingRadians()));

        	   // Se o inimigo está além de 200px, atire com força máxima, se não, reduza
        	   // a força do tiro para poupar energina em caso de erro
        	   if (e.getDistance() < 200) {
        	    setFire(3);
        	   } else {
        	    setFire(2.4);
        	   }

        	   // Calcula o angulo para o radar retornar e trava o radar
        	   double radarTurn = getHeadingRadians() + e.getBearingRadians() - getRadarHeadingRadians();
        	   setTurnRadarRightRadians(2 * Utils.normalRelativeAngle(radarTurn));

        	   // Se não tiver alvo, incrementa a variável de rotação
        	  } else if (targ != null) {
        	   spins++;
        	  }
        }
        
    }

    public void onRobotDeath(RobotDeathEvent e) {
    	if (getOthers() > 3) {
    		strategy = 1;
    	} else if (getOthers() > 1) {
    		strategy = 2;
    	} else if (getOthers() == 1) {
    		strategy = 3;
    	}
    	enemy.reset();
		radarDirection = 1;
		moveDirection = 1;

    }


    public void doMove() {

    	if(strategy == 1) {
    		double width = getBattleFieldWidth();
    		double height = getBattleFieldHeight();
    		//paredes
    		moveAmount = Math.max(width, height);
    		peek = false;
    		// turnleft para encontrar uma parede.
    			// getHeading() % 90 significa o resto de
    			// getHeading() dividido por 90.
    		turnLeft(getHeading() % 90);
    		ahead(moveAmount);
    		// Gira a arma para virar 90 graus à direita.
    		peek = true;
    		turnGunRight(90);
    		turnRight(90);

    		while (strategy == 1) {

    			moveAmount = calculaDistancia() - 25;

    			// Olha antes de virarmos quando o ahead () for concluído.
    			peek = true;
    			// Move-se pela parede fora
    			ahead(moveAmount);
    			// não espreita agora
    			peek = false;
    			// vira para a próxima parede
    			turnRight(90);
    			//onRobotDeath();
    		}
    	}
    	if(strategy==2) {
    		enemy.reset();
    	}
    }

    public void Move(ScannedRobotEvent e) {
		if(strategy == 2) {
    	if(enemy.none()==false) {
			//monitoramento de energia
			double diference = enemy.getEnergy() - e.getEnergy();
			if(getDistanceRemaining()== 0.0 &&  diference > 0.0 ){
				// vire ligeiramente em direção ao nosso inimigo
				setTurnRight(normalRelativeAngle(enemy.getBearing() + 90 - (15 * moveDirection)));
				if(0.0 < diference && diference < 0.5) {
					length = 19;
				}
				if(0.5 < diference && diference < 1.0) {
					length = 27;
				}
				if(1.0 < diference && diference < 1.5) {
					length = 33;
				}
				if(1.5 < diference && diference < 2.0) {
					length = 37;
				}
				if(2.0 < diference && diference < 2.5) {
					length = 48;
				}
				if(2.5 < diference && diference < 3) {
					length = 56;
				}
				else {
					length = 64;
				}
				setAhead(length*moveDirection);
				moveDirection *= -1;
			}
		}
		}
	}

    void doGun() {
		if (enemy.none()){
			return;
		}
		setTurnGunRight(normalRelativeAngle(getHeading() - getGunHeading() + enemy.getBearing()));
		if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10 && enemy.getDistance()<600) {
			setFire(400 / enemy.getDistance());
		}
	}

    void doRadar() {
		if (enemy.none()) {
			// olhe ao redor se não tivermos nenhum inimigo
			setTurnRadarRight(360);
			setTurnRadarLeft(360);
		} else {
			// oscila o radar
			double turn = getHeading() - getRadarHeading() + enemy.getBearing();
			turn += 30 * radarDirection;
			setTurnRadarRight(normalRelativeAngle(turn));
			radarDirection *= -1;
		}
	}

    public double calculaDistancia() {

        // Pega a altura e largura do campo de batalha e posição x,y do tanque
        double h = getBattleFieldHeight(); // Altura
        double w = getBattleFieldWidth();  // Largura
        double x = getX();
        double y = getY();

        // Pega a direção em que o tank se move e a sua posição atual (x, y) no campo de batalha
        double ang = getHeading(); // O ângulo está em graus, variando entre 0 (apontando pra cima) e 359) no sentido horário

        // Calcula os vetor normal de direção do tanque
        double dx = Math.sin(Math.toRadians(ang));
        double dy = Math.cos(Math.toRadians(ang));
        Double dir = new Point2D.Double(dx, dy);

        // Calcula o vetor do tanque em direção ao canto mais próximo da direção e sentido que ele segue
        dx = (dir.getX() > 0) ? w - x : -x;
        dy = (dir.getY() > 0) ? h - y : -y;
        Double canto = new Point2D.Double(dx, dy);

        // Calcula os angulos entre o vetor de direcao e os vetores dos os eixos x e y
        double angX = Math.acos(Math.abs(dir.getX()));
        double angY = Math.acos(Math.abs(dir.getY()));

        double distancia;
		// A distância é o cateto adjascente do menor ângulo
        if(angY < angX)
            distancia = Math.abs(canto.getY() / Math.cos(angY));
        else
            distancia = Math.abs(canto.getX() / Math.cos(angX));

        return distancia;
    }

    /**
	 * onHitRobot:  Move away a bit.
	 */
	public void onHitRobot(HitRobotEvent e) {
		// Se ele estiver na nossa frente, recua um pouco.
		if (e.getBearing() > -90 && e.getBearing() < 90) {
			back(100);
		} // senão ele está atrás de nós, então avança um pouco.
		else {
			ahead(100);
		}
	}



	 // Implementação da estratégia de movimento circular com fuga de paredes
	 public void circularMove(ScannedRobotEvent e) {
		 if(strategy==3) {
			 // Sempre se posiciona contra o nosso inimigo, virando um pouco para ele
			 setTurnRight(normalRelativeAngle(e.getBearing() + 90 - (15 * moveDirection)));

			 // mudar de direção se paramos (também se afasta da parede se estiver muito perto)
			 if (getVelocity() == 0) {
				 setMaxVelocity(8); // muda a velocidade para 8
				 moveDirection *= -1;
				 setAhead(10000 * moveDirection);
			 }
		 }
	 }
	// Calcula o rolamento absoluto entre dois pontos
	 // Na navegação, o rolamento absoluto é o ângulo no sentido horário entre o norte e um objeto 
	 // observado a partir do robô
	 double absoluteBearing(double x1, double y1, double x2, double y2) {
	  double xo = x2 - x1;
	  double yo = y2 - y1;
	  double hyp = Point2D.distance(x1, y1, x2, y2);
	  double arcSin = Math.toDegrees(Math.asin(xo / hyp));
	  double bearing = 0;

	  if (xo > 0 && yo > 0) { 
	   bearing = arcSin;
	  } else if (xo < 0 && yo > 0) { 
	   bearing = 360 + arcSin; 
	  } else if (xo > 0 && yo < 0) { 
	   bearing = 180 - arcSin;
	  } else if (xo < 0 && yo < 0) { 
	   bearing = 180 - arcSin; 
	  }

	  return bearing;
	 }
	
    public double normalRelativeAngle(double angle) {
        while (angle <= -180) {
        	angle += 360;
        }

        while (angle > 180) {
        	angle -= 360;
        }

        return angle;
    }

}