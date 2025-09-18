package com.lms.lms.repo;

import com.lms.lms.modals.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepo extends JpaRepository<Asset, String> {
}
