package com.lucene;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocRepo extends JpaRepository<Doc,Integer> {




}
