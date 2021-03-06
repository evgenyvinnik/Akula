/***********************************************************************************************************************
 *
 * Akula v2: A Toolset for Experimenting and Developing Thread Placement Algorithms on Multicore Systems
 * ==========================================
 *
 * Copyright (C) 2014 by Evgeny Vinnik and Sergey Blagodurov
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 **********************************************************************************************************************/
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
