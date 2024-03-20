package rishav.example.SpringRestDemo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import rishav.example.SpringRestDemo.model.Album;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    List<Album> findByAccount_Id(long id);
}
