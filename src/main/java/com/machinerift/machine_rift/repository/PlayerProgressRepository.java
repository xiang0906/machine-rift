package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.PlayerProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerProgressRepository extends JpaRepository<PlayerProgress, Long> {
}
