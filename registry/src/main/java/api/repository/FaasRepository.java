package api.repository;

import api.model.Faas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaasRepository extends JpaRepository<Faas, Long> {
  @Query("SELECT DISTINCT f.funcName FROM Faas f")
  List<String> findDistinctFuncNames();
}
