package com.synar.akula;
/*
 * Used to keep track of how threads were scheduled to be able to reconstruct.
 */
public class HistoryNode {

	public double		mTime;
	public int			mMemMomainId;
	public int			mMoreId;
	public HistoryNode mNext; //pointer to next node in list.
	
	/*
	 * Only used by the bootstrap module where degradations are known on
	 * a per tick basis.
	 */
	public double	mPerfDegrad;
 
}
