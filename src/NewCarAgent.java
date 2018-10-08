import java.awt.Color;


public class NewCarAgent implements Comparable<NewCarAgent> {
	protected static int _id_sequence = 0;
	protected int id;
	protected int pos;
	protected int pre_pos;
	protected int forward_pos;
	protected int speed=0;
	protected int next_fp=0;
	protected int pre_speed=0;
	protected int lane=0;
	protected int st=0;
	protected int before;
	protected  int back;

	protected Color color=new Color(0,0,0);

	protected int pred_delta=0;
	protected int delta_pos=0;

	protected int status=0;

	public NewCarAgent(int pos,int lane){
		this.id = _id_sequence;
		_id_sequence++;
		this.pos = pos;
		this.lane=lane;
	}

	@Override
	public int compareTo(NewCarAgent agent) {
		return this.pos-agent.pos;
	}

//	public class PosComparator implements Comparator<NewCarAgent>{
//		@Override
//		public int compare(NewCarAgent agent1,NewCarAgent agent2) {
//			return agent1.pos < agent2.pos ? -1 : 1;
//		}
//	}

	public void changeCar() {
		this.status=1;
	}
	public void changeBefore() {
		this.before=1;
	}
	public void changback() {
		this.back=1;
	}

	public void addSpeed(){
		speed++;
	}
	public void subSpeed(){
		speed--;
	}
	public void move(){
		pos=pos+speed;
	}
	public static void reset(){
		_id_sequence=0;
	}

	public void changeid(int id) {
		this.id=id;
	}

	public void changeLane(){
		if(lane==0){
			lane=1;
		}else if(lane==1){
			lane=0;
		}
		pos+=1;
	}
	public int nextLane(){
		if(lane==0){
			return 1;
		}else{
			return 0;
		}
	}


}

