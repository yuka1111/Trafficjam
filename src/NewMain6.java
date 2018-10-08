import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

public class NewMain6 {
	//	//seed1
	//	public static long SEED =1000000009;
	public static long SEED;
	//	//乱数
	//	private static Random random = new Random(SEED);
	public static Random random;
	//制限速度:v_limit
	public static int MAX_SPEED = 7; //(1で14km/h想定)
	public static int H = 2;
	//減速確率:p
	public static final double PRO_DEC = 0.00001;
	//道路長L
	public static final int CELL_NUM = 400;
	//レーン数
	public static final int LANE_NUM = 2;
	//経過時間（ターン）T
	public static final int TURN_NUM = 4000;
	//解消時
	public static int kaisyou=3999;
	//周回:step
	//public static final int STEP_NUM = 100;
	//平均取るための周回
	//public static int LOOP_NUM=10;
	public static int change_num= 0;
	public static int tounyu = 0;
	//車両密度:ρ
	public static double RO_NUM = 0.25;
	//エージェント数:N
	public static int CAR_AGENT_NUM = (int) ((RO_NUM) * (CELL_NUM));
	//前との車間距離:d1
	public static int delta_pos = 0;
	//前の前との車間距離:d2
	public static int pred_delta = 0;

	//前方車の予測スピード
	public static int pred_speed = 0;
	//各turnにおける平均速度
	public static double[] turn_ave_speed = new double[TURN_NUM];
	//速度加算したもの
	public static double all_speed = 0;
	//緩和車加算したもの
	public static double kanwa_all = 0;
	//普通車加算したもの
	public static double not_kanwa_all = 0;
	//時間平均
	public static double ave_speed = 0;
	//速度0の車の台数
	public static double[] zero_car = new double[TURN_NUM];
	//エージェント位置
	public static int[][] pos = new int[LANE_NUM][CAR_AGENT_NUM * LANE_NUM];
	//エージェントスピード
	public static int[][] speed = new int[LANE_NUM][CAR_AGENT_NUM * LANE_NUM];
	//緩和車平均速度
	public static double[] kanwa_speed = new double[TURN_NUM];
	//普通車平均速度
	public static double[] not_kanwa_speed = new double[TURN_NUM];
	public static int k = 0;
	public static int[][][] cell = new int[TURN_NUM + 1][LANE_NUM][CELL_NUM + 1];
	public static int[][][] cell_speed = new int[TURN_NUM + 1][LANE_NUM][CELL_NUM + 1];
	public static List<NewCarAgent> agents = new ArrayList<NewCarAgent>();
	public static List<NewCarAgent> Lane1 = new ArrayList<NewCarAgent>();
	public static List<NewCarAgent> Lane2 = new ArrayList<NewCarAgent>();
	private static int[][][] agent_cell= new int[TURN_NUM + 1][LANE_NUM][CELL_NUM + 1];;
	public static List<Integer> agentnum1 = new ArrayList<Integer>();
	public static List<Integer> agentnum2 = new ArrayList<Integer>();
	public static int k1= 0;
	public static int k2= 0;
	public static int k3= 0;
	public static int k4= 0;

	//public static double [][] all_F = new double[LOOP_NUM][STEP_NUM+2];

	public static void init(long s) {
		/******初期配置位置******/

		SEED = s;
		kaisyou=999;
		change_num=0;
		//乱数
		//private static Random random = new Random(SEED);
		random = new Random(s);
		//lane番目のi番目にいる車はcellno.何にいるか
		for (int lane = 0; lane < LANE_NUM; lane++) {
			for (int i = 0; i < CAR_AGENT_NUM; i++) {
				pos[lane][i] = random.nextInt(CELL_NUM);
				//System.out.println(i+":"+pos[lane][i]);
				for (int j = 0; j < i; j++) {
					if (pos[lane][j] == pos[lane][i]) {
						pos[lane][i] = random.nextInt(CELL_NUM);
						//System.out.println("retry");
						j = -1;
					}
				}
			}
		}

		//sort
		int[] sort_pos = new int[CAR_AGENT_NUM];
		for (int lane = 0; lane < LANE_NUM; lane++) {
			for (int i = 0; i < CAR_AGENT_NUM; i++) {
				sort_pos[i] = pos[lane][i];
			}
			Arrays.sort(sort_pos);
			for (int i = 0; i < CAR_AGENT_NUM; i++) {
				pos[lane][i] = sort_pos[i];
			}
		}

		//		for(int lane=0;lane<LANE_NUM;lane++){
		//		for (int i = 0 ; i < CAR_AGENT_NUM ; i++) {
		//			System.out.print(pos[lane][i]+" ");
		//			}
		//		System.out.println();
		//

		//		 List<Integer>sort=new ArrayList<Integer>();
		//			for(int lane=0;lane<LANE_NUM;lane++){
		//				for (int i = 0 ; i < CAR_AGENT_NUM ; i++) {
		//				sort.add(pos[lane][i]);
		//					}
		//				}
		//
		//		Collections.sort(sort);

		//		for(int i:sort)
		//System.out.println(i);

		/*for(int lane=0;lane<LANE_NUM;lane++){
			for (int i = 0 ; i < CAR_AGENT_NUM ; i++) {
				agents.add(new NewCarAgent(pos[lane][i],lane));
			}
			NewCarAgent.reset();
		}*/

		for (int lane = 0; lane < LANE_NUM; lane++) {
			for (int i = 0; i < CAR_AGENT_NUM; i++) {
				agents.add(new NewCarAgent(pos[lane][i], lane));
			}
			NewCarAgent.reset();
		}

		for (int lane = 0; lane < LANE_NUM; lane++) {
			for (int i = 0; i < CAR_AGENT_NUM; i++) {
				if (lane == 0)
					Lane1.add(new NewCarAgent(pos[lane][i], lane));
				if (lane == 1)
					Lane2.add(new NewCarAgent(pos[lane][i], lane));
			}
			NewCarAgent.reset();
		}

		Collections.sort(agents);
		for(int turn=0;turn<TURN_NUM+1;turn++) {
			for(int lane=0;lane<LANE_NUM;lane++)
				for(int c=0;c<CELL_NUM+1;c++)
					cell[turn][lane][c]=0;

		}

//				for(NewCarAgent agent:agents) {
//					System.out.printf(agent.pos+" ");
//				}
//				System.out.println();

		/********初期配置ここまで*******/

		//初期速度
		int j = 0;
		for (NewCarAgent agent : agents) {
			speed[agent.lane][agent.id] = agent.speed;
		}
		//前の車の初期位置情報
		j = 0;
		for (NewCarAgent agent : agents) {
			j++;
			if (j == CAR_AGENT_NUM) {
				j = 0;
			}
			if (agent.id == CAR_AGENT_NUM - 1) {
				agent.forward_pos = pos[agent.lane][0];
				//前の車の初期速度
				agent.pre_speed = speed[agent.lane][0];
			} else {
				agent.forward_pos = pos[agent.lane][agent.id + 1];
				//前の車の初期速度
				agent.pre_speed = speed[agent.lane][agent.id + 1];
			}
		}

		//前の前の車の初期位置情報
		for (NewCarAgent agent : agents) {
			if (agent.id == CAR_AGENT_NUM - 2) {
				agent.next_fp = pos[agent.lane][0];
			} else if (agent.id == CAR_AGENT_NUM - 1) {
				agent.next_fp = pos[agent.lane][1];
			} else {
				agent.next_fp = pos[agent.lane][agent.id + 2];
			}
		}
	}

	public static void run() {
		/*********走行*********/
		//初期値つっこむ
		for (NewCarAgent agent : agents) {
			cell[0][agent.lane][agent.pos] = 1;
		}

		for (int turn = 0; turn < TURN_NUM; turn++) {
			//System.out.println("turn："+turn);
			k = 0;
			all_speed = 0;
			not_kanwa_all=0;
			kanwa_all=0;
			//Iterator<NewCarAgent> Agent =agents.iterator();
			//while(Agent.hasNext()) {
			//NewCarAgent agent=Agent.next();
			for (NewCarAgent agent : agents) {
				//エージェントをセルに入れる
				//cell[turn][agent.lane][agent.pos]=1;
				//前の車両との距離
				if (agent.forward_pos < agent.pos) {
					delta_pos = (agent.forward_pos + CELL_NUM) - agent.pos - 1;
				} else {
					delta_pos = Math.abs(agent.forward_pos - agent.pos) - 1;
				}
//				if(delta_pos<0)System.out.println(agent.forward_pos+"d"+agent.pos);
				//前の前の車両との距離
				if (agent.next_fp < agent.forward_pos) {
					pred_delta = (agent.next_fp + CELL_NUM) - agent.forward_pos - 1;
				} else {
					pred_delta = Math.abs(agent.next_fp - agent.forward_pos) - 1;
				}
//				if(pred_delta<0)System.out.println(agent.forward_pos+"p"+agent.next_fp);

				//test
				//if(agent.lane==1){
				//System.out.println("id:"+agent.id+". pos:"+agent.pos+". fw:"+agent.forward_pos+". next:"+agent.next_fp+". delta:"+delta_pos+". pred_delta:"+pred_delta+". speed:"+agent.speed);
				//}

				//平均速度のために速度を足して保存
				all_speed = all_speed + agent.speed;
				not_kanwa_all += agent.speed;
				if(agent.status==1) {
				kanwa_all+=agent.speed;
				not_kanwa_all=all_speed-agent.speed;
				}

				/*------------速度規制テスト--------
				if(turn>100 && turn<300 && agent.pos>200 && agent.pos<300){
					MAX_SPEED=2;
				}else{
					MAX_SPEED=5;
				}
				-------------------------------------*/
				//change lane したら先行車情報がわからなくなる
				//そこの調整が必要
				//idは絶対的なものとした方がよいのでは？
				//				double G=0;
				//				G=0.15*(agent.speed)+0.0097*(agent.speed)*(agent.speed);
				//				if(agent.delta_pos<G&&agent.status!=1) {
				//					boolean change=true;
				//					int num=agents.indexOf(agent);
				//					int lane=agent.nextLane();
				//
				//					if(lane==1&&cell[turn-1][lane][num+1]==1) {
				//						change=false;
				//					}else if(lane==0&&cell[turn][lane][num+1]==1) {
				//						change=false;
				//					}
				//					else{
				//						for(int i=1;i<=5;i++) {
				//							if(cell[turn][lane][num-i]==1)
				//								change=false;
				//						}
				//					}
				//
				//					if(change) {agent.changeLane();
				//
				//					System.out.print("change:"+agent.id+",");}
				//
				//				}

				/*******走行ルール********/
				//rule1:加速
				int speed1 = agent.speed;
				agent.speed = Math.min(speed1 + 1, MAX_SPEED);

				//rule2:拡張NSモデル減速
				if (agent.speed > delta_pos) {
					pred_speed = Math.max(Math.min(Math.min(pred_delta - 1, agent.pre_speed), MAX_SPEED - 1), 0);
					if(agent.speed<0) System.out.println(1);
					agent.speed = Math.min(agent.speed, delta_pos + pred_speed);
//					if(agent.speed<0) System.out.println(2+";"+delta_pos+";"+pred_speed);
				} else {
					agent.speed = agent.speed;
				}
				//rule2':渋滞緩和車
				/*if(agent.status==1) {
					System.out.println("id:"+agent.id+"lane:"+agent.lane+"pos:"+agent.pos);
					int lane=agent.lane;
					int num=agent.pos;
					int n=0;
					for(int i=1;i<=7;i++) {
						if(num+i>CELL_NUM||num+i<0) n=num%CELL_NUM;
						if(cell[turn][lane][n]!=0) {
							for (NewCarAgent agent2 : agents) {
								if(agent2.id==n&&agent2.lane==lane&&agent2.speed<=H) {
									agent.speed=Math.max(1,agent.speed-1);
									System.out.println("kanwa:"+agent2.id);
									break;
								}
							}
						}
				}
				}*/
				//視野範囲調整
				if (agent.status == 1) {
					//System.out.println("kanwa"+agent.id);
					int a = agent.id;
					int b;
					int p[] = new int[20];
					int c = pos[agent.lane][a];
					//System.out.println("id:"+a+" pos:"+c);

					for (int i = 1; i < 10; i++) {
						if (c + i < CELL_NUM)
							p[i] = c + i;
						else
							p[i] = c + i - CELL_NUM;
					}

					for (int i = 1; i < 10; i++) {
						if(cell[turn][agent.lane][p[i]]==1) {
							if(cell_speed[turn][agent.lane][p[i]]<=H) {
								int v;
								v=agent.speed;
								//速度が1より大きいとき
								if(agent.speed>1) {
								agent.speed = Math.max(0, agent.speed - 1);
//								System.out.println("kanwa"+v+"to"+agent.speed);
								break;}
							}
						}
					}

//					for (int i = 1; i < 8; i++) {
//						if (a + i < CAR_AGENT_NUM)
//							b = a + i;
//						else
//							b = a + i - CAR_AGENT_NUM;
//						for (int j = 1; j < 8; j++) {
////							if(pos[agent.lane][b]==p[j]) {
////							{System.out.println("id:"+b+"pos:"+pos[agent.lane][b]+" spd:"+speed[agent.lane][b]);}
//							if (speed[agent.lane][b] <= H) {//数字が大きいときの7つ先の値を調整
//								//agent.speed=agent.speed-1;
//								int v;
//								v=agent.speed;
//								agent.speed = Math.max(0, agent.speed - 1);
//								System.out.println("kanwa"+v+"to"+agent.speed);
//								//System.out.println(agent.id+"'s speed="+agent.speed);
//								i = 8 - 1;
//								j = 8 - 1;
//							}
////						}
//						}
//					}
				}
				//rule2:NSモデル減速
				//agent.speed=Math.min(agent.speed, delta_pos);

				//rule3:確率減速
								if(PRO_DEC*100000>(int)(random.nextInt(100000))){
									if(agent.status!=1) {
									agent.speed=Math.max(agent.speed-1, 0);
//									System.out.println("dec");
									}
								}
				//rule4:移動
				//ここでアニメーションの描画処理やればいける
				agent.move();
				/*******走行ルールここまで********/

				//道ループ（周期）処理
				if (agent.pos >= CELL_NUM) {
					agent.pos = agent.pos - CELL_NUM;
				}

				//				//はやさ出力
				//				System.out.print(turn+1+" :");
				//				System.out.print(delta_pos+" :");
				//				System.out.print(pred_delta+" :");
				//				System.out.print(agent.lane+" :");
				//				System.out.print(agent.pos+" :");
				//				System.out.println(agent.speed);
				int jstatus = 0;
				if (turn == TURN_NUM - 2 || turn == TURN_NUM - 1) {
					if (agent.speed == 0) {
						jstatus = 1;
						//System.out.print(" juutai:" + agent.speed);
						//System.exit(1);
					}
				}
				if (agent.lane == 0)
					for (NewCarAgent agent1 : Lane1) {
						if (agent1.id == agent.id) {
							agent1.pos = agent.pos;
						}
					}

				else if (agent.lane == 1)
					for (NewCarAgent agent2 : Lane2) {
						if (agent2.id == agent.id) {
							agent2.pos = agent.pos;
						}
					}
if(agent.speed<0) {
	System.out.println("Error");
//System.out.println(turn+1+" "+agent.lane+" "+agent.id+" "+agent.pos+" "+agent.speed+" "+speed1+" "+ agent.pre_speed +" "+delta_pos +" "+pred_speed);
	}
				cell[turn + 1][agent.lane][agent.pos] = 1;
				if (agent.status==1) {
					agent_cell[turn+1][agent.lane][agent.pos]=1;
					if(agent.lane==0&&agent.back==1)k1=agent.pos;
					if(agent.lane==0&&agent.before==1)k2=agent.pos;
					if(agent.lane==1&&agent.back==1)k3=agent.pos;
					if(agent.lane==1&&agent.before==1)k4=agent.pos;
				}else {
					agent_cell[turn+1][agent.lane][agent.pos]=-1;
					}
			}

			//agentの位置全更新終了
			//			for(int i=0;i<LANE_NUM;i++) {
			//				for(int j=0;j<CELL_NUM;j++) {
			//					if(cell[turn+1][i][j]==1)
			//					System.out.print(j+" ");
			//				}
			//				System.out.println();
			//			}
			//			Collections.sort(agents);
			//			for(NewCarAgent agent:agents) {
			//				System.out.print(agent.pos+" ");
			//			}
			//			System.out.println();

			//変更禁止区域

			if(tounyu==1) {
			int n1=0;
			int n2=0;

			n1=k1;
			n2=k2;
//			System.out.println(n1+";"+n2);
			if(n1<n2) {
			for(int j=n1+1;j<n2;j++)
				agent_cell[turn+1][0][j]=100;
			}else {
				for(int j=n2;j<CELL_NUM;j++)
					agent_cell[turn+1][0][j]=100;
				for(int j=0;j<n1;j++)
					agent_cell[turn+1][0][j]=100;
			}
			agentnum1.clear();

			n1=k3;
			n2=k4;
//			System.out.println(n1+";"+n2);
			if(n1<n2) {
			for(int j=n1+1;j<n2;j++)
				agent_cell[turn+1][1][j]=100;
			}else {
				for(int j=n2;j<CELL_NUM;j++)
					agent_cell[turn+1][1][j]=100;
				for(int j=0;j<n1;j++)
					agent_cell[turn+1][1][j]=100;
			}
			agentnum2.clear();
			}

//		for(int lane=0;lane<LANE_NUM;lane++) {
//			for(int i=0;i<CELL_NUM;i++) {
//				int num=0;
//				int state=0;
//				if(agent_cell[turn+1][lane][i]==1) {
//					state=1;
//					num=i;
//					i++;
//					while(state==1) {
//						if(agent_cell[turn+1][lane][i]==-1)
//							state=0;
//						else if(agent_cell[turn+1][lane][i]==1) {
//							for(int j=num+1;j<i;j++)
//								agent_cell[turn+1][lane][j]=100;
//							state=0;
//						}else if(i==CELL_NUM-1) {
//							for(k=0;k<num;k++) {
//							if(agent_cell[turn+1][lane][i]==-1) {
//								break;}
//							else if(agent_cell[turn+1][lane][i]==1) {
//								for(int j=num+1;j<i;j++)
//									agent_cell[turn+1][lane][j]=100;
//								state=0;
//							}
//							}
//						}else{
//							i++;
//						}
//					}
//				}
//
//			}
//		}

			//車線変更の有無
			ListIterator<NewCarAgent> Agent = agents.listIterator();
			while (Agent.hasNext()) {
				NewCarAgent agent = Agent.next();
				//NewCarAgent agent2=Agent.next();
				//previous使う？
				//for(NewCarAgent agent:agents) {
				double G = 0;
				G = 0.15 * (agent.speed) + 0.0097 * (agent.speed) * (agent.speed);
//				G=-1;
				int now_lane = agent.lane;
				int num = agent.pos;
				int num1=num+1;
				int num2=num+2;
				if(num+1>CELL_NUM)num1=num+2-CELL_NUM;
				if(num+2>CELL_NUM)num2=num+2-CELL_NUM;
				//if(agent2.lane!=now_lane) agent.delta_pos=agent2.pos-agent.pos;
				//if(agent.delta_pos<G&&agent.status!=1)
				if ((G > 0 && cell[turn + 1][now_lane][num1] == 1 && agent.speed <= 7 && agent.status != 1)
						|| (G > 0 && cell[turn + 1][now_lane][num2] == 1 && agent.speed > 5 && agent.status != 1)) {

					boolean change = true;
					//int num=agent.pos;
					//int now_lane=agent.lane;
					int to_lane = agent.nextLane();
					int n = 0;
					int next = 0;

					if (num + 1 >= CELL_NUM)
						next = num - CELL_NUM;
					else
						next = num;
					if (cell[turn + 1][to_lane][num] == 1) {
						change = false;
					} else if (cell[turn + 1][to_lane][next + 1] == 1) {
						change = false;
					} else {
						for (int i = 1; i <= 5; i++) {
							if (num - i < 0)
								n = num + CELL_NUM;
							else
								n = num;
							if (cell[turn + 1][to_lane][n - i] == 1)
								change = false;
						}
					if (agent_cell[turn+1][to_lane][num]==100) {
//						System.out.println("aaa");
						change=false;
						}
					}
					if (change) {
						change_num++;
						agent.changeLane();
						if (now_lane == 0) {
							Lane2.add(new NewCarAgent(next + 1, to_lane));
							//System.out.println(next);
							for (NewCarAgent agent1 : Lane1) {
							if(agent1.pos==next)
							agent1.changeLane();}
						}
						if (now_lane == 1) {
							Lane1.add(new NewCarAgent(next + 1, to_lane));
							//System.out.println(next);
							for (NewCarAgent agent2 : Lane2) {
							if(agent2.pos==next)
							agent2.changeLane();
						}
						}
						cell[turn + 1][now_lane][num] = 0;
						cell[turn + 1][to_lane][next + 1] = 1;
//						System.out.print("to:"+to_lane+"change:"+agent.pos+",");
					}

				}
			}

			//			System.out.println();
			//			System.out.println("size"+Lane1.size());
			//			System.out.println("size"+Lane2.size());

			//			for(NewCarAgent agent:Lane1) {
			//				if(agent.lane!=0) Lane1.remove(agent);
			//
			//			}
			//			for(NewCarAgent agent:Lane2) {
			//				if(agent.lane!=1) Lane2.remove(agent);
			//
			//			}



//			for(NewCarAgent agent2:Lane2) {
//				System.out.print(agent2.lane+" ");
//				}
//			System.out.println();

			Iterator<NewCarAgent> l1 = Lane1.iterator();
			while (l1.hasNext()) {
				NewCarAgent agent1 = l1.next();
				if (agent1.lane != 0)
					l1.remove();
			}
			Iterator<NewCarAgent> l2 = Lane2.iterator();
			while (l2.hasNext()) {
				NewCarAgent agent2 = l2.next();
				if (agent2.lane != 1)
					l2.remove();
			}

			Collections.sort(Lane1);
			Collections.sort(Lane2);

			/*			for(int lane=0;lane<LANE_NUM;lane++) {
							for(int i = 1; i <= CELL_NUM; i++){
								if(cell[turn][lane][i]>0){
									System.out.print("■");
								}else{
									System.out.print("□");
								}
							}
							System.out.println();
						}
						*/
			//			System.out.println();
			//			for(int lane=0;lane<LANE_NUM;lane++){
			//				for (int i = 0 ; i < CAR_AGENT_NUM ; i++) {
			//					System.out.print(pos[lane][i]+" ");
			//					}
			//				System.out.println();
			//				}

			/*for(int lane=0;lane<LANE_NUM;lane++){
				for (int i = 0 ; i < CELL_NUM ; i++) {
					if(cell[turn][lane][i]!=0)
					System.out.print(i+" ");
					}
				System.out.println();
				}*/

			/*for(int lane=0;lane<LANE_NUM;lane++){
				for (int i = 0 ; i < CAR_AGENT_NUM ; i++) {
					for (int j = 0 ; j < CAR_AGENT_NUM ; j++) {
						if(pos[lane][i]==pos[lane][j]&&i!=j) {
							System.out.println("Error!");
							break;
						}
					}
					}
				}*/

			//turn毎の平均速度
			turn_ave_speed[turn] = all_speed / (CAR_AGENT_NUM * LANE_NUM);


			int k_num=0;
			for (NewCarAgent agent : agents)
				if (agent.status == 1) {
					k_num++;
				}
			if(k_num==0||turn>kaisyou) {
				kanwa_speed[turn]=-1;
				//turnごとの緩和車平均速度
			}
			else {
				not_kanwa_speed[turn]=not_kanwa_all/((CAR_AGENT_NUM * LANE_NUM)-k_num);
				kanwa_speed[turn]=kanwa_all/k_num;
			}

			//Lane1の変更

			for (int i = 0; i < Lane1.size(); i++) {
				NewCarAgent agent1 = Lane1.get(i);
				if (i == Lane1.size() - 1) {
					agent1.forward_pos = Lane1.get(0).pos;
					agent1.pre_speed = Lane1.get(0).speed;
					agent1.changeid(i);
					//agents.add(agent);
				} else {
					agent1.forward_pos = Lane1.get(i + 1).pos;
					agent1.pre_speed = Lane1.get(i + 1).speed;
					agent1.changeid(i);
					//agents.add(agent);
				}
			}
			for (int i = 0; i < Lane1.size(); i++) {
				NewCarAgent agent1 = Lane1.get(i);
				if (i == Lane1.size() - 2) {
					agent1.next_fp = Lane1.get(0).pos;
				} else if (i == Lane1.size() - 1) {
					agent1.next_fp = Lane1.get(1).pos;
				} else {
					agent1.next_fp = Lane1.get(i + 2).pos;
				}
			}
			//Lane2の変更
			NewCarAgent.reset();
			for (int i = 0; i < Lane2.size(); i++) {
				NewCarAgent agent1 = Lane2.get(i);
				if (i == Lane2.size() - 1) {
					agent1.forward_pos = Lane2.get(0).pos;
					agent1.pre_speed = Lane2.get(0).speed;
					agent1.changeid(i);
					//agents.add(agent);
				} else {
					agent1.forward_pos = Lane2.get(i + 1).pos;
					agent1.pre_speed = Lane2.get(i + 1).speed;
					agent1.changeid(i);
					//agents.add(agent);
				}
			}
			for (int i = 0; i < Lane2.size(); i++) {
				NewCarAgent agent1 = Lane2.get(i);
				if (i == Lane2.size() - 2) {
					agent1.next_fp = Lane2.get(0).pos;
				} else if (i == Lane2.size() - 1) {
					agent1.next_fp = Lane2.get(1).pos;
				} else {
					agent1.next_fp = Lane2.get(i + 2).pos;
				}
			}
			agents.clear();
			NewCarAgent.reset();
			for (NewCarAgent agent1 : Lane1) {
				agents.add(agent1);
			}
			NewCarAgent.reset();
			for (NewCarAgent agent1 : Lane2) {
				agents.add(agent1);
			}

			Collections.sort(agents);

			for (NewCarAgent agent : agents) {
				//情報保存
				pos[agent.lane][agent.id] = agent.pos;
				speed[agent.lane][agent.id] = agent.speed;
				cell_speed[turn+1][agent.lane][agent.pos]=agent.speed;
			}

			//			int l=0;
			//			int n=0;
			//			for( int i=0;i<LANE_NUM;i++) {
			//				for( int j=0;j<CELL_NUM;j++) {
			//				if(cell[turn+1][i][j]==1&&cell[turn+1][i][j+1]==1) {
			//					//System.out.print("juutai"+" "+j+"&"+(j+1)+",");
			//					l=i+1;
			//					n=j;
			//					i=LANE_NUM-1;
			//					j=CELL_NUM-1;
			//					}
			//				}
			//				//System.out.println();
			//			}

			//			for(int i =0;i<LANE_NUM;i++) {
			//			for (NewCarAgent agent : agents){
			//				int a =agent.id;
			//					for (NewCarAgent agent2 : agents)
			//						if(agent2.id!=a-1&&pos[i][agent2.id]==pos[i][a]+1) {System.out.print("juutai"+" "+pos[i][agent.id]+"&"+pos[i][agent2.id]+",");
			//						}
			//			}System.out.println();
			//			}

			//投入！
			//int l=-1;
			int l = 0;
			int au = 1;
			//if(Lane1.size()<Lane2.size())l=1;
			if (tounyu != 1 && turn > TURN_NUM/10 && l != -1) {
				int kanwa1 = random.nextInt(Lane1.size() - au);
				int kanwa2 = random.nextInt(Lane1.size() - au);
				int kanwa3 = random.nextInt(Lane1.size() - au);
				int kanwa4 = random.nextInt(Lane1.size() - au);
				int kanwa5 = random.nextInt(Lane1.size() - au);
				int kanwa6 = random.nextInt(Lane1.size() - au);
				int kanwa7 = random.nextInt(Lane1.size() - au);
				int kanwa8 = random.nextInt(Lane1.size() - au);
				int kanwa9 = random.nextInt(Lane1.size() - au);
				int kanwa10 = random.nextInt(Lane1.size() - au);
				//int kanwa6=random.nextInt(Lane1.size()-au);
				//			System.out.println(n+"+"+l);
				for (NewCarAgent agent1 : Lane1) {
					//				System.out.print(" "+agent1.pos);
//					if(agent1.id==0)agent1.changeCar();
//					if(agent1.id==2)agent1.changeCar();

										if(agent1.id>=0&&agent1.id<=0+au) agent1.changeCar();
										if(agent1.id==0)agent1.changback();
										if(agent1.id==1)agent1.changeBefore();
//										if(agent1.id>=8&&agent1.id<=8+au) agent1.changeCar();
//										if(agent1.id>=10&&agent1.id<=10+au) agent1.changeCar();
//										if(agent1.id>=16&&agent1.id<=16+au) agent1.changeCar();
//										if(agent1.id>=24&&agent1.id<=24+au) agent1.changeCar();
					//					if(agent1.id>=32&&agent1.id<=32+au) agent1.changeCar();
//										if(agent1.id>=40&&agent1.id<=40+au) agent1.changeCar();
//										if(agent1.id>=47&&agent1.id<=47+au) agent1.changeCar();
//					if (agent1.id >= 54 && agent1.id <= 54 + au)
//						agent1.changeCar();
//										if(agent1.id>=62&&agent1.id<=62+au) agent1.changeCar();
					//					if(agent1.id>=70&&agent1.id<=70+au) agent1.changeCar();
					//					if(agent1.id>=80&&agent1.id<=80+au) agent1.changeCar();
//					if (agent1.id >= 90 && agent1.id <= 90 + au)
//						agent1.changeCar();

					//					if(agent1.id>=kanwa6&&agent1.id<=kanwa6+au) agent1.changeCar();
					//					if(agent1.id>=kanwa7&&agent1.id<=kanwa7+au) agent1.changeCar();
					//					if(agent1.id>=kanwa8&&agent1.id<=kanwa8+au) agent1.changeCar();
					//					if(agent1.id>=kanwa9&&agent1.id<=kanwa9+au) agent1.changeCar();
//										if(agent1.id>=kanwa10&&agent1.id<=kanwa10+au) agent1.changeCar();
					//				if(agent1.id==67) agent1.changeCar();
					//				if(agent1.id==kanwa1+1)agent1.changeCar();
					//				if(agent1.id==kanwa1+2) agent1.changeCar();
					//				if(agent1.id==kanwa1+3)agent1.changeCar();
					//				if(agent1.id==kanwa1+4) agent1.changeCar();
					//				if(agent1.id==kanwa1+5)agent1.changeCar();

					//				if(agent1.pos==n)agent1.changeCar();
					//				if(agent1.pos==n+1)agent1.changeCar();
				}
				//System.out.println();
				//id 0とid 1にとりあえずつっこむだけでも両レーンなら解消する　時間比較？
				for (NewCarAgent agent2 : Lane2) {
//					if(agent2.id==0)agent2.changeCar();
//					if(agent2.id==2)agent2.changeCar();

										if(agent2.id>=0&&agent2.id<=0+au) agent2.changeCar();
										if(agent2.id==0)agent2.changback();
										if(agent2.id==1)agent2.changeBefore();
//										if(agent2.id>=8&&agent2.id<=8+au) agent2.changeCar();
//										if(agent2.id>=16&&agent2.id<=16+au) agent2.changeCar();
//									if(agent2.id>=24&&agent2.id<=24+au) agent2.changeCar();
//										if(agent2.id>=32&&agent2.id<=32+au) agent2.changeCar();
//										if(agent2.id>=40&&agent2.id<=40+au) agent2.changeCar();
					//					if(agent2.id>=47&&agent2.id<=47+au) agent2.changeCar();
//										if(agent2.id>=54&&agent2.id<=54+au) agent2.changeCar();
//										if(agent2.id>=62&&agent2.id<=62+au) agent2.changeCar();
					//					if(agent2.id>=70&&agent2.id<=70+au) agent2.changeCar();
//									if(agent2.id>=80&&agent2.id<=80+au) agent2.changeCar();
					//					if(agent2.id>=90&&agent2.id<=90+au) agent2.changeCar();

					//				if(agent2.id==67) agent2.changeCar();
					//////				if(agent2.id==kanwa2+1)agent2.changeCar();
					////				//if(agent2.pos==n+1)agent2.changeCar();
				}
				tounyu = 1;
			}

			//			//前の車の位置情報更新
			//			for (NewCarAgent agent : agents){
			//				if(agent.id==CAR_AGENT_NUM-1){
			//					agent.forward_pos=pos[agent.lane][0];
			//					//速度更新
			//					agent.pre_speed=speed[agent.lane][0];
			//				}else{
			//					agent.forward_pos=pos[agent.lane][agent.id+1];
			//					//速度更新
			//					agent.pre_speed=speed[agent.lane][agent.id+1];
			//				}
			//			}
			//			//前の前の車の位置情報更新
			//			for (NewCarAgent agent : agents){
			//				if(agent.id==CAR_AGENT_NUM-2){
			//					agent.next_fp=pos[agent.lane][0];
			//				}else if(agent.id==CAR_AGENT_NUM-1){
			//					agent.next_fp=pos[agent.lane][1];
			//				}else{
			//					agent.next_fp=pos[agent.lane][agent.id+2];
			//				}
			//			}

//			System.out.println(turn);

//			for(NewCarAgent agent1:Lane1) {
//				if(agent1.lane==1)System.out.print(agent1.pos+":"+"("+agent1.id+")"+" ");}
			//System.out.println();
//			System.out.println(Lane1.size());
//
//			for(NewCarAgent agent2:Lane2) {
//				if(agent2.lane==0)System.out.print(agent2.pos+":"+"("+agent2.id+")"+" ");
//				System.out.print(agent2.pos+" ");
//				}
			//System.out.println();
//			System.out.println(agents.size());

			double add_speed = 0;
			for (int j = 0; j < turn; j++) {
				add_speed = add_speed + turn_ave_speed[j];
			}
			ave_speed = add_speed / turn;
			//System.out.println("時間平均速度："+ave_speed);
			//流量F：ρ×時間平均速度
			double F = RO_NUM * ave_speed;
			//System.out.println("流量："+F);
			//if(F>1.2)System.out.println("完了"+F+":"+turn);
//緩和車解消のチェック
			int justatus = 0;
			double add_speed1 = 0;
			double ave_speed1 = 0;
			if (turn > TURN_NUM/10 && tounyu == 1) {
				for (NewCarAgent agent : agents)
					if (agent.speed == 0)	justatus = 1;
//0の判断に緩和車は含む？アベレージ＞２？
//					if (agent.speed == 0&&agent.status!=1)
//						justatus = 1;
				if (justatus == 0) {//速さ0の車が存在しないとき
					for (int j = turn - 2; j < turn; j++) {
						add_speed1 = add_speed1 + turn_ave_speed[j];
					}
					ave_speed1 = add_speed1 / 2.0;
					//System.out.println("時間平均速度："+ave_speed);
					//流量F：ρ×時間平均速度
//					F = RO_NUM * ave_speed1;
					//System.out.println("流量："+F);
					//System.out.println("kanwa");
					for (NewCarAgent agent1 : agents)
						if (agent1.status == 1)
							agent1.status = 0;
					tounyu = 0;
				}
			}
			int c=0;
			for (NewCarAgent agent : agents)
				if (agent.speed ==0) {
					c++;
				}
			zero_car[turn]=c;

			for (NewCarAgent agent : agents)
				if (agent.speed <0) {
					System.out.println("error");
				}
//			System.out.println(zero_car[turn]);
			//System.out.println(turn+":"+c);

//			add_speed=0;
//			for (int j = 0; j < turn; j++) {
//				add_speed = add_speed + turn_ave_speed[j];
//			}
//			ave_speed = add_speed / turn;
//			System.out.println(turn+":"+c+":"+(double)ave_speed);
			if(turn>400&&zero_car[turn-4]==0&&zero_car[turn-3]==0&&zero_car[turn-2]==0&&zero_car[turn-1]==0&&zero_car[turn]==0) {
				if(turn<kaisyou)kaisyou=turn;
			}
//速さの分布
//			if(turn==465||turn==466) {
//			System.out.println(turn+"tic");
//			for(NewCarAgent agent1:Lane1) {
//				if(agent1.status==1)
//					System.out.printf("["+agent1.speed+"]"+" ");
//				else
//				System.out.printf(agent1.speed+" ");
//			}
//			System.out.println();
//			for(NewCarAgent agent1:Lane1) {
//				if(agent1.status==1)
//					System.out.printf("["+agent1.pos+"]"+" ");
//				else
//				System.out.printf(agent1.pos+" ");
//			}
//			System.out.println();
//			for(NewCarAgent agent1:Lane1) {
//				if(agent1.status==1)
//					System.out.printf("["+agent1.next_fp+";"+agent1.forward_pos+";"+agent1.pre_speed+"] ");
//				else
//				System.out.printf(agent1.next_fp+";"+agent1.forward_pos+";"+agent1.pre_speed+" ");
//			}
//			System.out.println();
//			for(NewCarAgent agent2:Lane2) {
//				if(agent2.status==1)
//					System.out.printf("["+agent2.pos+"]"+" ");
//				else
//				System.out.printf(agent2.pos+" ");
//			}
//			System.out.println();
//
//			for(NewCarAgent agent2:Lane2) {
//				if(agent2.status==1)
//					System.out.printf("["+agent2.speed+"]"+" ");
//				else
//				System.out.printf(agent2.speed+" ");
//			}
//			System.out.println();
//			for(NewCarAgent agent2:Lane2) {
//				if(agent2.status==1)
//					System.out.printf("["+agent2.next_fp+";"+agent2.forward_pos+";"+agent2.pre_speed+"] ");
//				else
//				System.out.printf(agent2.next_fp+";"+agent2.forward_pos+";"+agent2.pre_speed+" ");
//			}
//			System.out.println();
//			for(NewCarAgent agent1:Lane1) {
//				if(agent1.status==1)
//					System.out.printf("["+agent1.pos+"]"+" ");
//			}
//			System.out.println();
//			for(NewCarAgent agent2:Lane2) {
//				if(agent2.status==1)
//					System.out.printf("["+agent2.pos+"]"+" ");
//			}
//			System.out.println();
//			for(NewCarAgent agent1:Lane1) {
//				System.out.printf(agent1.pos+" ");
//			}
//			System.out.println();
//			}
			if(turn<1770) {
//			System.out.println(Lane1.size());
				double add_speed11 = 0;
				for (NewCarAgent agent1 : Lane2) {
					add_speed11 += agent1.speed;}
				ave_speed = add_speed11 / Lane2.size();
//				System.out.println(ave_speed);
				//流量F：ρ×時間平均速度
				//System.out.println("流量："+F);
//				System.out.println(Lane2.size());
				F = ave_speed * Lane2.size() / CELL_NUM;
//				System.out.println(F);

			}

//			System.out.println("L1 "+Lane1.size());
//			System.out.println("L2 "+Lane2.size());


//			double add_speedl1 = 0;
////			if(turn<1400) {
//			for (NewCarAgent agent2 : Lane2)
//				add_speedl1 += agent2.speed;
//			ave_speed = add_speedl1 / Lane1.size();
//			F = ave_speed * Lane2.size() / CELL_NUM;
//			System.out.println(F);
//			}

		}
		/*********走行終わり*********/

//		System.out.println();
//		int count1 = 0;
//		for (NewCarAgent agent1 : Lane1) {
//			if (agent1.status == 1)
//				count1++;
//		}
//		System.out.println("(" + count1 + ")");
//		int count2 = 0;
//		for (NewCarAgent agent2 : Lane2) {
//			if (agent2.status == 1)
//				count2++;
//		}
//		System.out.println("(" + count2 + ")");
//		System.out.println(Lane1.size());
//		System.out.println(Lane2.size());
	}

	public static void calc() {
		//全体
		double add_speed = 0;
//		System.out.print(turn_ave_speed[7]+" ");
		for (int j = 0; j < TURN_NUM; j++) {
			add_speed = add_speed + turn_ave_speed[j];
		}
		ave_speed = add_speed / TURN_NUM;
//		System.out.println(ave_speed);
		double F = RO_NUM * ave_speed;
//		System.out.println("all:"+F);
//		System.out.println(F);
//		add_speed = 0;
//		for (int j = 0; j < 1354; j++) {
//			add_speed = add_speed + turn_ave_speed[j];
//		}
//		ave_speed = add_speed / TURN_NUM;
//		System.out.println(ave_speed);

		//ラスト2ターン
		add_speed = 0;
		for (int j = TURN_NUM - 2; j < TURN_NUM; j++) {
			add_speed = add_speed + turn_ave_speed[j];
		}
//		System.out.println(zero_car[TURN_NUM-1]);

		ave_speed = add_speed / 2.0;
//		System.out.println(ave_speed);
//		System.out.println( turn_ave_speed[TURN_NUM-1]);
//		System.out.println(" last2時間平均速度："+ave_speed);
		//流量F：ρ×時間平均速度
		 F = RO_NUM * ave_speed;
//		 System.out.println(zero_car[TURN_NUM-1]+" "+zero_car[TURN_NUM-2]+" "+zero_car[TURN_NUM-3]+" "+zero_car[TURN_NUM-4]+" "+zero_car[TURN_NUM-5]);
		System.out.println(F);
//		 System.out.print(F);
//		System.out.println("last2:"+F);

		double add_speed1 = 0;
		for (NewCarAgent agent1 : Lane1)
			add_speed1 += agent1.speed;
		ave_speed = add_speed1 / Lane1.size();
		//System.out.println("時間平均速度："+ave_speed);
		//流量F：ρ×時間平均速度
		//System.out.println("流量："+F);
		F = ave_speed * Lane1.size() / CELL_NUM;
//		System.out.print(" lane1:"+F);

		double add_speed2 = 0;
		for (NewCarAgent agent2 : Lane2)
			add_speed2 += agent2.speed;
		ave_speed = add_speed2 / Lane2.size();
		//System.out.println("時間平均速度："+ave_speed);
		//流量F：ρ×時間平均速度
		//System.out.println("流量："+F);
		F = ave_speed * Lane2.size() / CELL_NUM;
//		System.out.println(" lane2:"+F);
//		if(kaisyou<4000)
//		System.out.println(kaisyou);
//		System.out.println(zero_car[TURN_NUM-1]);

//		if(tounyu==0) {
		int num=0;
		for (int j = 0; j < kaisyou; j++) {
			if(kanwa_speed[j]>=0) {
			add_speed = add_speed + kanwa_speed[j];
			num++;
			}
		}

		ave_speed = add_speed / num;
//		System.out.println("緩和平均:"+ave_speed+","+num);
//		System.out.println(ave_speed);

		add_speed=0;
		for (int j = 0; j < kaisyou; j++) {
			if(kanwa_speed[j]>=0) {
			add_speed = add_speed + not_kanwa_speed[j];
			}
		}
		ave_speed = add_speed / num;
//		System.out.println("普通車平均:"+ave_speed);
//		System.out.println(ave_speed);
//		add_speed=0;
//		for (int j = 0; j < kaisyou; j++) {
//			if(kanwa_speed[j]>=0) {
//			add_speed = add_speed + turn_ave_speed[j];
//			}
//		}
//		ave_speed = add_speed / num;
//		System.out.println("全体平均:"+ave_speed);
//		System.out.println(ave_speed);
		}

//	}

	//----------------------------
	//System.out.println("STEP"+step+"  RO:"+RO_NUM);
	/*	public static void input_csv(){
			try {
	        //出力先を作成する
	        FileWriter fw = new FileWriter("C:\\Users\\Yuka\\Desktop\\test\\testes.csv", false);  //※１
	        PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
	    	for(int j=0;j<LOOP_NUM;j++){
	    		pw.print("loop"+j);
	    		for(int i=0;i<=STEP_NUM;i++){
		            	pw.print(all_F[j][i]+",");
			            //pw.println();
		            	//pw.print("");
		            }
		            pw.println();
	        }
	        System.out.println("出力完了");
	        pw.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	*/
	   /**

     * RandomTableからseedc番目の乱数を取り出し返す。

     * @return

     */
    public static long getSeedFormRandomTable(int seedc) {

            String stringseedc= "" + seedc;

            try {
            		File file = new File("src/Random.table100");
                    BufferedReader br= new BufferedReader(new FileReader(file));
                    while(true) {
                            String s= br.readLine();

                            if(s == null) {
                                    br.close(); return(0);
                            }
                            String[] breakstring= s.split("[ :,\t\n]");

                            if(breakstring.length == 2) {

                                    if(stringseedc.equals(breakstring[0])) {
                                            br.close();

return(Long.parseLong(breakstring[1]));
                                    }
                            }
                    }
            } catch (FileNotFoundException e) {
                    e.printStackTrace();
            } catch (IOException e) {
                    e.printStackTrace();
            }
            throw new Error();
    }


	public static void main(String[] args) {

		int i=Integer.parseInt(args[0]);
		//seed<-table
		//public static long SEED = getSeedFromRandomTable(arga[0]);
		System.out.println(RO_NUM);
		init(getSeedFormRandomTable(i));
		run();
//		System.out.println(i);
//		System.out.println(change_num);
		calc();
		agents.clear();
		Lane1.clear();
		Lane2.clear();
		tounyu = 0;


//		init(getSeedFormRandomTable(5));
//		run();
//		System.out.println(5);
//		System.out.println(change_num);
//		calc();
//		agents.clear();
//		Lane1.clear();
//		Lane2.clear();
//		tounyu = 0;
//
//
//
//		init(getSeedFormRandomTable(27));
//		run();
//		System.out.println(27);
//		System.out.println(change_num);
//		calc();
//		agents.clear();
//		Lane1.clear();
//		Lane2.clear();
//		tounyu = 0;
//
//		init(getSeedFormRandomTable(30));
//		run();
//		System.out.println(30);
//		System.out.println(change_num);
//		calc();
//		agents.clear();
//		Lane1.clear();
//		Lane2.clear();
//		tounyu = 0;
//
//		init(getSeedFormRandomTable(36));
//		run();
//		System.out.println(36);
//		System.out.println(change_num);
//		calc();
//		agents.clear();
//		Lane1.clear();
//		Lane2.clear();
//		tounyu = 0;
//
//		init(getSeedFormRandomTable(38));
//		run();
//		System.out.println(38);
//		System.out.println(change_num);
//		calc();
//		agents.clear();
//		Lane1.clear();
//		Lane2.clear();
//		tounyu = 0;
//
//		init(getSeedFormRandomTable(45));
//		run();
//		System.out.println(45);
//		System.out.println(change_num);
//		calc();
//		agents.clear();
//		Lane1.clear();
//		Lane2.clear();
//		tounyu = 0;
//
//		init(getSeedFormRandomTable(51));
//		run();
//		System.out.println(51);
//		System.out.println(change_num);
//		calc();
//		agents.clear();
//		Lane1.clear();
//		Lane2.clear();
//		tounyu = 0;
//
//		init(getSeedFormRandomTable(53));
//		run();
//		System.out.println(53);
//		System.out.println(change_num);
//		calc();
//		agents.clear();
//		Lane1.clear();
//		Lane2.clear();
//		tounyu = 0;
//
//		init(getSeedFormRandomTable(59));
//		run();
//		System.out.println(59);
//		System.out.println(change_num);
//		calc();
//		agents.clear();
//		Lane1.clear();
//		Lane2.clear();
//		tounyu = 0;
//
//		init(getSeedFormRandomTable(64));
//		run();
//		System.out.println(64);
//		System.out.println(change_num);
//		calc();
//		agents.clear();
//		Lane1.clear();
//		Lane2.clear();
//		tounyu = 0;
//
//		init(getSeedFormRandomTable(70));
//		run();
//		System.out.println(70);
//		System.out.println(change_num);
//		calc();
//		agents.clear();
//		Lane1.clear();
//		Lane2.clear();
//		tounyu = 0;
//

		for (int j = i+1; j <=100; j++) {
			init(getSeedFormRandomTable(j));
			run();
//			System.out.println(j);
//			System.out.println(change_num);
			calc();
			agents.clear();
			Lane1.clear();
			Lane2.clear();
			tounyu = 0;
		}




	}
}
