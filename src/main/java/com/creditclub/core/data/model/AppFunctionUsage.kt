package com.creditclub.core.data.model

import androidx.room.*

@Entity(tableName = "app_function_usage")
class AppFunctionUsage(@PrimaryKey var fid: Int = 0) {
    var usage = 1
}

@Dao
interface AppFunctionUsageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(appFunctionUsage: AppFunctionUsage)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(appFunctionUsage: AppFunctionUsage)

    @Query("DELETE FROM app_function_usage")
    fun deleteAll()

    @Query("SELECT * from app_function_usage order by usage desc limit 3")
    fun getMostUsed(): List<AppFunctionUsage>

    @Query("SELECT * from app_function_usage where fid=:fid")
    fun getFunction(fid: Int): AppFunctionUsage?
}
