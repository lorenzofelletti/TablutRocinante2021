package it.unibo.ai.didattica.competition.tablut.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.unibo.ai.didattica.competition.tablut.domain.State;

public class ClientControllerAshtonTablut implements ClientController {

	private List<String> citadels;
	
	public ClientControllerAshtonTablut() {
		super();
		this.citadels = new ArrayList<String>();
		//this.strangeCitadels = new ArrayList<String>();
		this.citadels.add("a4");
		this.citadels.add("a5");
		this.citadels.add("a6");
		this.citadels.add("b5");
		this.citadels.add("d1");
		this.citadels.add("e1");
		this.citadels.add("f1");
		this.citadels.add("e2");
		this.citadels.add("i4");
		this.citadels.add("i5");
		this.citadels.add("i6");
		this.citadels.add("h5");
		this.citadels.add("d9");
		this.citadels.add("e9");
		this.citadels.add("f8");
		this.citadels.add("e8");
	}

	@Override
	public boolean canMove(State state, int row, int column) {
		return (this.canMoveUp(state, row, column) || this.canMoveDown(state, row, column) || this.canMoveLeft(state, row, column) || this.canMoveRight(state, row, column));
	}
	
	private boolean canMoveUp(State state, int row, int column) {
		if(row==0)
		{
			return false;
		}
		if(!state.getPawn(row-1, column).equalsPawn(State.Pawn.EMPTY.toString()) || (this.citadels.contains(state.getBox(row-1, column))&& !this.citadels.contains(state.getBox(row, column))))
		{
			return false;
		}
		return true;		
	}

	private boolean canMoveDown(State state, int row, int column) {
		if(row==state.getBoard().length-1)
		{
			return false;
		}
		if(!state.getPawn(row+1, column).equalsPawn(State.Pawn.EMPTY.toString()) || (this.citadels.contains(state.getBox(row+1, column))&& !this.citadels.contains(state.getBox(row, column))))
		{
			return false;
		}
		return true;	
	}
	
	private boolean canMoveLeft(State state, int row, int column) {
		if(column==0)
		{
			return false;
		}
		if(!state.getPawn(row, column-1).equalsPawn(State.Pawn.EMPTY.toString()) || (this.citadels.contains(state.getBox(row, column-1)) && !this.citadels.contains(state.getBox(row, column))))
		{
			return false;
		}
		return true;	
	}
	
	private boolean canMoveRight(State state, int row, int column) {
		if(column==state.getBoard().length-1)
		{
			return false;
		}
		if(!state.getPawn(row, column+1).equalsPawn(State.Pawn.EMPTY.toString()) || (this.citadels.contains(state.getBox(row, column+1)) && !this.citadels.contains(state.getBox(row, column))))
		{
			return false;
		}
		return true;
	}
	
	@Override
	public String chooseDestination(State state, int row, int column) {
		int direction = this.chooseDirection(state, row, column);
		switch (direction) {
		case 1:
			System.out.println("Direzione su");
			break;
		case 2:
			System.out.println("Direzione giu");
			break;
		case 3:
			System.out.println("Direzione sinistra");
			break;
		case 4:
			System.out.println("Direzione destra");
			break;
		default:
			break;
		}
		int lenght = this.chooseLenght(state, row, column, direction);
		int col;
		int rig;
		String ret="";
		
		//mi muovo sopra
		if(direction==1)
		{
			col=column;
			rig=row-lenght;
			ret=state.getBox(rig, col);
		}
		//mi muovo sotto
		if(direction==2)
		{
			col=column;
			rig=row+lenght;
			ret=state.getBox(rig, col);
		}
		//mi muovo a sinistra
		if(direction==3)
		{
			col=column-lenght;
			rig=row;
			ret=state.getBox(rig, col);
		}
		//mi muovo a destra
		if(direction==4)
		{
			col=column+lenght;
			rig=row;
			ret=state.getBox(rig, col);
		}
		return ret;
	}

	private int chooseDirection(State state, int row, int column) {
		List<Integer> check = new ArrayList<Integer>();
		if(this.canMoveUp(state, row, column))
		{
			check.add(1);
		}
		if(this.canMoveDown(state, row, column))
		{
			check.add(2);
		}
		if(this.canMoveLeft(state, row, column))
		{
			check.add(3);
		}
		if(this.canMoveRight(state, row, column))
		{
			check.add(4);
		}
		int ret = 0;
		if(check.size()!=0)
		{
			Random random = new Random();
			ret=check.get(random.nextInt(check.size()));
			//System.out.println("Possibili direzioni "+check.size());
		}
		return ret;
	}
	
	private int chooseLenght(State state, int row, int column, int dir)
	{
		int lenght=0;
		boolean canMoveOnCitadel;
		if(this.citadels.contains(state.getBox(row, column)))
		{
			canMoveOnCitadel = true;
		}
		else
		{
			canMoveOnCitadel = false;
		}
		//se mi muovo sopra
		if(dir==1)
		{
			int count=0;
			for(int i=1; count==0; i++)
			{
				if(row-i>=0)
				{
					if(state.getPawn(row-i, column).equalsPawn(State.Pawn.EMPTY.toString()))
					{
						if(this.citadels.contains(state.getBox(row-i, column)) && canMoveOnCitadel)
						{
							lenght++;
						}
						if(this.citadels.contains(state.getBox(row-i, column)) && !canMoveOnCitadel)
						{
							count++;
						}
						if(!this.citadels.contains(state.getBox(row-i, column)))
						{
							lenght++;
							canMoveOnCitadel = false;
						}
					}
					if(!state.getPawn(row-i, column).equalsPawn(State.Pawn.EMPTY.toString()))
					{
						count++;
					}
				}
				else
				{
					count++;
				}
			}
		}
		
		//se mi muovo sotto
		if(dir==2)
		{
			int count=0;
			for(int i=1; count==0; i++)
			{
				if(row+i<state.getBoard().length)
				{
					if(state.getPawn(row+i, column).equalsPawn(State.Pawn.EMPTY.toString()))
					{
						if(this.citadels.contains(state.getBox(row+i, column)) && canMoveOnCitadel)
						{
							lenght++;
						}
						if(this.citadels.contains(state.getBox(row+i, column)) && !canMoveOnCitadel)
						{
							count++;
						}
						if(!this.citadels.contains(state.getBox(row+i, column)))
						{
							lenght++;
							canMoveOnCitadel = false;
						}
					}
					if(!state.getPawn(row+i, column).equalsPawn(State.Pawn.EMPTY.toString()))
					{
						count++;
					}
				}
				else
				{
					count++;
				}
			}
		}
		
		
		//se mi muovo a sinistra
		if(dir==3)
		{
			int count=0;
			for(int i=1; count==0; i++)
			{
				if(column-i>=0)
				{
					if(state.getPawn(row, column-i).equalsPawn(State.Pawn.EMPTY.toString()))
					{
						if(this.citadels.contains(state.getBox(row, column-i)) && canMoveOnCitadel)
						{
							lenght++;
						}
						if(this.citadels.contains(state.getBox(row, column-i)) && !canMoveOnCitadel)
						{
							count++;
						}
						if(!this.citadels.contains(state.getBox(row, column-i)))
						{
							lenght++;
							canMoveOnCitadel = false;
						}
					}
					if(!state.getPawn(row, column-i).equalsPawn(State.Pawn.EMPTY.toString()))
					{
						count++;
					}
				}
				else
				{
					count++;
				}
			}
		}
		
		//se mi muovo a destra
		if(dir==4)
		{
			int count=0;
			for(int i=1; count==0; i++)
			{
				if(column+i<state.getBoard().length)
				{
					if(state.getPawn(row, column+i).equalsPawn(State.Pawn.EMPTY.toString()))
					{
						if(this.citadels.contains(state.getBox(row, column+i)) && canMoveOnCitadel)
						{
							lenght++;
						}
						if(this.citadels.contains(state.getBox(row, column+i)) && !canMoveOnCitadel)
						{
							count++;
						}
						if(!this.citadels.contains(state.getBox(row, column+i)))
						{
							lenght++;
							canMoveOnCitadel = false;
						}
					}
					if(!state.getPawn(row, column+i).equalsPawn(State.Pawn.EMPTY.toString()))
					{
						count++;
					}
				}
				else
				{
					count++;
				}
			}
		}
		System.out.println("Dist "+lenght);
		
		if(lenght==1)
		{
			return 1;
		}
		return (new Random().nextInt(lenght-1))+1;
	}
	
}
