# List users in shazam_db database running in Shazam-Database Docker Container

docker exec -it shazam-database psql -U shazam -d shazam_db -c '\du'

# List databases with shazam_db database running in Shazam-Database Docker Container

docker exec -it shazam-database psql -U shazam -d shazam_db -c '\l'
