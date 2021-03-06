package com.skkzas.superherosightings.dao;

import com.skkzas.superherosightings.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Shazena Khan, Kristina Zakharova, Arfin Shah
 *
 * Date Created: Sep 25, 2020
 */
@Repository
public class SuperheroDaoDB implements SuperheroDao {

    @Autowired
    JdbcTemplate jdbc;

    @Override
    public Superhero getSuperheroById(int id) {
        try {
            final String GET_SUPERHERO_BY_ID = "SELECT * FROM Superhero "
                    + "WHERE SuperheroId = ?";
            Superhero superhero = jdbc.queryForObject(GET_SUPERHERO_BY_ID, new SuperheroMapper(), id);
            superhero.setPower(getPowerForSuperhero(superhero.getSuperheroId()));
            return superhero;
        } catch (DataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Superhero> getAllSuperheros() {
        final String GET_ALL_SUPERHEROS = "SELECT * FROM Superhero";
        List<Superhero> allSuperheroes = jdbc.query(GET_ALL_SUPERHEROS, new SuperheroMapper());
        for (Superhero superhero : allSuperheroes) {
            superhero.setPower(getPowerForSuperhero(superhero.getSuperheroId()));
        }
        return allSuperheroes;
    }

    @Transactional
    @Override
    public Superhero addSuperhero(Superhero superhero) {
        final String INSERT_SUPERHERO = "INSERT INTO Superhero"
                + "(SuperheroName, Description, PowerId, PhotoFileName) "
                + "VALUES(?,?,?,?)";

        jdbc.update(INSERT_SUPERHERO,
                superhero.getSuperheroName(),
                superhero.getSuperheroDescription(),
                superhero.getPower().getPowerId(),
                superhero.getPhotoFileName());

        int newId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        superhero.setSuperheroId(newId);

        /**
         * TODO Maybe in front-end, on the add Super page, you can allow the
         * user to choose an existing or add a new power then you would have to
         * process that first in the service and then add it to the powerDao if
         * necessary, then add the power to the superhero, then add the super
         * here.*
         */
        return superhero;
    }

    @Override
    public void updateSuperhero(Superhero superhero) {
        final String UPDATE_Superhero = "UPDATE Superhero SET "
                + "SuperheroName = ?, "
                + "Description = ?, "
                + "PowerId = ? ,"
                + "PhotoFileName = ? "
                + "WHERE SuperheroId = ?";

        jdbc.update(UPDATE_Superhero,
                superhero.getSuperheroName(),
                superhero.getSuperheroDescription(),
                superhero.getPower().getPowerId(),
                superhero.getPhotoFileName(),
                superhero.getSuperheroId());
    }

    @Transactional
    @Override
    public void deleteSuperheroById(int id) {
        try {
            final String DELETE_SIGHTING = "DELETE FROM Sighting "
                    + "WHERE SuperheroId = ?";
            jdbc.update(DELETE_SIGHTING, id);
        } catch (DataAccessException e) {
        }

        try {
            final String DELETE_SUPERHEROORGANIZATION = "DELETE FROM SuperheroOrganization "
                    + "WHERE SuperheroId = ?";
            jdbc.update(DELETE_SUPERHEROORGANIZATION, id);

        } catch (DataAccessException e) {
        }

        final String DELETE_SUPERHERO = "DELETE FROM Superhero "
                + "WHERE SuperheroId = ?";
        jdbc.update(DELETE_SUPERHERO, id);

    }

    @Override
    public List<Superhero> getAllSuperheroesForLocation(Location location) {
        final String SELECT_SUPERHEROES_FOR_LOCATION = "SELECT * FROM Superhero su "
                + "JOIN Sighting si ON su.SuperheroId = si.SuperheroId "
                + "JOIN Location l ON si.LocationId = l.LocationId "
                + "WHERE l.LocationId = ? "
	+ "GROUP BY su.superheroID ";
        List<Superhero> superheroesForLocation = jdbc.query(SELECT_SUPERHEROES_FOR_LOCATION, new SuperheroMapper(), location.getLocationId());
        for (Superhero superhero : superheroesForLocation) {
            superhero.setPower(getPowerForSuperhero(superhero.getSuperheroId()));
        }
        return superheroesForLocation;
    }

    @Override
    public List<Superhero> getAllSuperherosForOrganization(Organization organization) {
        final String SELECT_SUPERHEROES_FOR_ORGANIZATION = "SELECT * FROM Superhero s "
                + "JOIN SuperheroOrganization so ON s.SuperheroId = so.SuperheroId WHERE so.OrgId = ?";
        List<Superhero> superheroesForOrganization = jdbc.query(SELECT_SUPERHEROES_FOR_ORGANIZATION, new SuperheroMapper(), organization.getOrgId());
        for (Superhero superhero : superheroesForOrganization) {
            superhero.setPower(getPowerForSuperhero(superhero.getSuperheroId()));
        }
        return superheroesForOrganization;
    }

    //Method to get all supers with that power
    @Override
    public List<Superhero> getAllSuperheroesWithThatPower(int powerId) {
        final String SELECT_SUPERHEROES_WITH_POWER = "SELECT * FROM Superhero WHERE PowerId = ?";

        List<Superhero> superheroesWithThatPower = jdbc.query(SELECT_SUPERHEROES_WITH_POWER, new SuperheroMapper(), powerId);
        for (Superhero superhero : superheroesWithThatPower) {
            superhero.setPower(getPowerForSuperhero(superhero.getSuperheroId()));
        }

        return superheroesWithThatPower;
    }

    @Override
    public Superhero getSuperheroForSighting(int sightingId) {
        try {
            final String GET_SUPERHERO_FOR_SIGHTING = "SELECT * FROM Superhero s "
                    + "JOIN Sighting si ON s.SuperheroId = si.SuperheroId "
                    + "WHERE si.SightingId = ?";
            Superhero superheroForSighting = jdbc.queryForObject(GET_SUPERHERO_FOR_SIGHTING, new SuperheroMapper(), sightingId);

            superheroForSighting.setPower(getPowerForSuperhero(superheroForSighting.getSuperheroId()));

            return superheroForSighting;
        } catch (DataAccessException e) {
            return null;
        }
    }

    //helper method
    private Power getPowerForSuperhero(int superheroId) {
        final String SELECT_POWER_FOR_SUPER = "SELECT p.PowerId, p.PowerName FROM Power p "
                + "JOIN Superhero s ON s.PowerId = p.PowerId WHERE SuperheroId = ?";

        Power powerForSuperhero = jdbc.queryForObject(SELECT_POWER_FOR_SUPER, new PowerDaoDB.PowerMapper(), superheroId);

        return powerForSuperhero;
    }

    public static final class SuperheroMapper implements RowMapper<Superhero> {

        @Override
        public Superhero mapRow(ResultSet rs, int index) throws SQLException {
            Superhero superhero = new Superhero();
            superhero.setSuperheroId(rs.getInt("SuperheroId"));
            superhero.setSuperheroName(rs.getString("SuperheroName"));
            superhero.setSuperheroDescription(rs.getString("Description"));
            superhero.setPhotoFileName(rs.getString("photoFileName"));

            return superhero;
        }
    }
}
