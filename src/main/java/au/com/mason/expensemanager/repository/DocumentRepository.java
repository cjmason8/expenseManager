package au.com.mason.expensemanager.repository;

import au.com.mason.expensemanager.domain.Document;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface DocumentRepository extends CrudRepository<Document, Long> {

    List<Document> findByFolderPath(String folderPath);

    @Query("SELECT a FROM Document a WHERE a.folderPath = :folderPath AND a.fileName = :fileName")
    Document getFolder(String folderPath, String folderName);

    @Modifying
    @Query("UPDATE Document set folderPath = replace(folderPath, ':oldPath', ':newPath')")
    void updateDirectoryPaths(String oldPath, String newPath);

    @Query("DELETE from Document where folderPath LIKE ':folderPath%'")
    void deleteDirectory(String folderPath);
}
