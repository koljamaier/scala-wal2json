#!/bin/sh
export config_file=/usr/share/postgresql/postgresql.conf
pg_recvlogical -d postgres -U postgres --slot test_slot --create-slot -P wal2json
echo "host    all             all             0.0.0.0/0            trust" >> /var/lib/postgresql/data/pg_hba.conf
exit