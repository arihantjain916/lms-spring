package com.lms.lms.repo;

import com.lms.lms.modals.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepo extends JpaRepository<Product, String> {

}
