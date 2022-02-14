/*
 * Copyright 2020 Dash Core Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.schildbach.wallet.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * @author Samuel Barbosa
 */
@Dao
abstract class BlockchainStateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(blockchainState: BlockchainState)

    fun save(blockchainState: BlockchainState) {
        if (blockchainState.replaying && blockchainState.percentageSync == 100) {
            blockchainState.replaying = false
        }
        insert(blockchainState)
    }

    @Query("SELECT * FROM blockchain_state LIMIT 1")
    abstract fun load(): LiveData<BlockchainState?>

    @Query("SELECT * FROM blockchain_state LIMIT 1")
    abstract fun loadSync(): BlockchainState?

    @Query("SELECT * FROM blockchain_state LIMIT 1")
    abstract suspend fun get(): BlockchainState?
}