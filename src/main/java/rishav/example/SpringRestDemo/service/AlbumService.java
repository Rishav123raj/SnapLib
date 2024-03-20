package rishav.example.SpringRestDemo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rishav.example.SpringRestDemo.model.Album;
import rishav.example.SpringRestDemo.repository.AlbumRepository;

@Service
public class AlbumService {

    @Autowired
    private AlbumRepository albumRepository;

    public Album save(Album album) {
        return albumRepository.save(album);
    }

    public List<Album> findByAccount_Id(long id) {
        return albumRepository.findByAccount_Id(id);
    }

    public Optional<Album> findById(long id) {
        return albumRepository.findById(id);
    }
}
