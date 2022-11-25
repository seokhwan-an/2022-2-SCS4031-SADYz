package SADYz.backend.client.service;

import SADYz.backend.client.domain.Client;
import SADYz.backend.client.domain.DoorClosedTime;
import SADYz.backend.client.domain.LastMovedTime;
import SADYz.backend.client.dto.ClientDto;
import SADYz.backend.client.dto.DoorClosedTimeDto;
import SADYz.backend.client.dto.LastMovedTimeDto;
import SADYz.backend.client.repository.ClientRepository;
import SADYz.backend.client.repository.DoorClosedTimeRepository;
import SADYz.backend.client.repository.LastMovedTimeRepository;
import SADYz.backend.global.S3.s3Uploader.s3Uploader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ClientService {

  private final ClientRepository clientRepository;
  private final LastMovedTimeRepository lastMovedTimeRepository;
  private final DoorClosedTimeRepository doorClosedTimeRepository;

  @Autowired
  private s3Uploader s3Uploader;

  public Client addClient(ClientDto clientDto){
    Client client = clientDto.toEntity();
    return clientRepository.save(client);
  }

  public LastMovedTime addLastMovedTime(String phoneNumber, LastMovedTimeDto lastMovedTimeDto){
    Client client = clientRepository.findByPhonenumber(phoneNumber);
    LastMovedTimeDto newLastMovedTimeDto = LastMovedTimeDto.builder()
        .lastMovedTime(lastMovedTimeDto.getLastMovedTime())
        .location(lastMovedTimeDto.getLocation())
        .client(client)
        .build();
    return lastMovedTimeRepository.save(newLastMovedTimeDto.toEntity());
  }

  public DoorClosedTime addDoorClosedTime(String phoneNumber, DoorClosedTimeDto doorClosedTimeDto){
    Client client = clientRepository.findByPhonenumber(phoneNumber);
    DoorClosedTimeDto newDoorClosedTimeDto = DoorClosedTimeDto.builder()
        .doorClosedTime(doorClosedTimeDto.getDoorClosedTime())
        .client(client)
        .build();
    return doorClosedTimeRepository.save(newDoorClosedTimeDto.toEntity());
  }

  public Client updateClient(Long id,ClientDto clientDto){
    Client client = clientRepository.findById(id).orElseThrow(
        ()->new IllegalArgumentException("해당 id가 없습니다")
    );
    client.update(clientDto);
    return clientRepository.save(client);
  }
  public LastMovedTime updateLastMovedTime(String phoneNumber, LastMovedTimeDto lastMovedTimeDto){
    Client client = clientRepository.findByPhonenumber(phoneNumber);
    LastMovedTime lastMovedTime = lastMovedTimeRepository.findByClient(client);
    lastMovedTime.updateLastMovedTime(client,lastMovedTimeDto);
    return lastMovedTimeRepository.save(lastMovedTime);
  }

  public DoorClosedTime updateDoorClosedTime(String phoneNumber, DoorClosedTimeDto doorClosedTimeDto){
    Client client = clientRepository.findByPhonenumber(phoneNumber);
    DoorClosedTime doorClosedTime = doorClosedTimeRepository.findByClient(client);
    doorClosedTime.updateDoorClosedTime(client,doorClosedTimeDto);
    return doorClosedTimeRepository.save(doorClosedTime);
  }

  public ClientDto readClient(Long id){
    Client client = clientRepository.findById(id).orElseThrow(
        ()->new IllegalArgumentException("해당 id가 없습니다")
    );
    ClientDto clientDto = Client.EntitytoDto(client);
    return clientDto;
  }

  public List<ClientDto> readAllClient(){
    List<ClientDto> clientDtos = new ArrayList<>();
    List<Client> clients = clientRepository.findAll();
    for (Client client : clients){
      ClientDto clientDto = Client.EntitytoDto(client);
      clientDtos.add(clientDto);
    }
    return clientDtos;
  }

  public DoorClosedTimeDto readDoorClosedTime(String phoneNumber){
    Client client = clientRepository.findByPhonenumber(phoneNumber);
    DoorClosedTime doorClosedTime = doorClosedTimeRepository.findByClient(client);
    return DoorClosedTimeDto.toDto(doorClosedTime);
  }

  public void deleteClient(Long id){
    Client client = clientRepository.findById(id).orElseThrow(
        ()->new IllegalArgumentException("해당 id가 없습니다")
    );
    clientRepository.delete(client);
  }

  public Client uploadS3Image(Long id,MultipartFile image) throws IOException {
    Client client = clientRepository.findById(id).orElseThrow(
        ()->new IllegalArgumentException("해당 id가 없습니다")
    );
    if(!image.isEmpty()){
      String storedFileName = s3Uploader.upload(image,"images");
      client.updateImageUrl(client,storedFileName);
    }
    return clientRepository.save(client);
  }

  public Client deleteS3Image(Long id){
    Client client = clientRepository.findById(id).orElseThrow(
        ()->new IllegalArgumentException("해당 id가 없습니다")
    );
    client.deleteImageUrl(client);
    return clientRepository.save(client);
  }
}
