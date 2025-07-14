-- Create permissions table
CREATE TABLE permissions (
                             id UUID PRIMARY KEY,
                             name VARCHAR(255) NOT NULL UNIQUE,
                             description TEXT,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             created_by VARCHAR(255),
                             updated_at TIMESTAMP,
                             updated_by VARCHAR(255),
                             deleted_at TIMESTAMP,
                             deleted_by VARCHAR(255)
);

-- Create roles table
CREATE TABLE roles (
                       id UUID PRIMARY KEY,
                       name VARCHAR(255) NOT NULL UNIQUE,
                       description TEXT,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       created_by VARCHAR(255),
                       updated_at TIMESTAMP,
                       updated_by VARCHAR(255),
                       deleted_at TIMESTAMP,
                       deleted_by VARCHAR(255)
);

-- Create role_permissions table
CREATE TABLE role_permissions (
                                  role_id UUID NOT NULL,
                                  permission_id UUID NOT NULL,
                                  PRIMARY KEY (role_id, permission_id),
                                  FOREIGN KEY (role_id) REFERENCES roles(id),
                                  FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

-- Create users table
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       is_active BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       created_by VARCHAR(255),
                       updated_at TIMESTAMP,
                       updated_by VARCHAR(255),
                       deleted_at TIMESTAMP,
                       deleted_by VARCHAR(255)
);

-- Create user_roles table
CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role_id UUID NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users(id),
                            FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Create indexes for users
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_deleted_at ON users(deleted_at) WHERE deleted_at IS NULL;

-- Create indexes for roles
CREATE INDEX idx_roles_name ON roles(name);
CREATE INDEX idx_roles_created_at ON roles(created_at);
CREATE INDEX idx_roles_deleted_at ON roles(deleted_at) WHERE deleted_at IS NULL;

-- Create indexes for permissions
CREATE INDEX idx_permissions_name ON permissions(name);
CREATE INDEX idx_permissions_created_at ON permissions(created_at);
CREATE INDEX idx_permissions_deleted_at ON permissions(deleted_at) WHERE deleted_at IS NULL;